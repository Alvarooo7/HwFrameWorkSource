package com.android.i18n.phonenumbers;

import com.android.i18n.phonenumbers.Phonemetadata.PhoneMetadata;
import com.android.i18n.phonenumbers.Phonemetadata.PhoneNumberDesc;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.android.i18n.phonenumbers.internal.MatcherApi;
import com.android.i18n.phonenumbers.internal.RegexBasedMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShortNumberInfo {
    private static final ShortNumberInfo INSTANCE = new ShortNumberInfo(RegexBasedMatcher.create());
    private static final Set<String> REGIONS_WHERE_EMERGENCY_NUMBERS_MUST_BE_EXACT = new HashSet();
    private static final Logger logger = Logger.getLogger(ShortNumberInfo.class.getName());
    private final Map<Integer, List<String>> countryCallingCodeToRegionCodeMap = CountryCodeToRegionCodeMap.getCountryCodeToRegionCodeMap();
    private final MatcherApi matcherApi;

    public enum ShortNumberCost {
        TOLL_FREE,
        STANDARD_RATE,
        PREMIUM_RATE,
        UNKNOWN_COST
    }

    static {
        REGIONS_WHERE_EMERGENCY_NUMBERS_MUST_BE_EXACT.add("BR");
        REGIONS_WHERE_EMERGENCY_NUMBERS_MUST_BE_EXACT.add("CL");
        REGIONS_WHERE_EMERGENCY_NUMBERS_MUST_BE_EXACT.add("NI");
    }

    public static ShortNumberInfo getInstance() {
        return INSTANCE;
    }

    ShortNumberInfo(MatcherApi matcherApi) {
        this.matcherApi = matcherApi;
    }

    private List<String> getRegionCodesForCountryCode(int countryCallingCode) {
        List<String> regionCodes = (List) this.countryCallingCodeToRegionCodeMap.get(Integer.valueOf(countryCallingCode));
        return Collections.unmodifiableList(regionCodes == null ? new ArrayList(0) : regionCodes);
    }

    private boolean regionDialingFromMatchesNumber(PhoneNumber number, String regionDialingFrom) {
        return getRegionCodesForCountryCode(number.getCountryCode()).contains(regionDialingFrom);
    }

    public boolean isPossibleShortNumberForRegion(PhoneNumber number, String regionDialingFrom) {
        if (!regionDialingFromMatchesNumber(number, regionDialingFrom)) {
            return false;
        }
        PhoneMetadata phoneMetadata = MetadataManager.getShortNumberMetadataForRegion(regionDialingFrom);
        if (phoneMetadata == null) {
            return false;
        }
        return phoneMetadata.getGeneralDesc().getPossibleLengthList().contains(Integer.valueOf(getNationalSignificantNumber(number).length()));
    }

    public boolean isPossibleShortNumber(PhoneNumber number) {
        List<String> regionCodes = getRegionCodesForCountryCode(number.getCountryCode());
        int shortNumberLength = getNationalSignificantNumber(number).length();
        for (String region : regionCodes) {
            PhoneMetadata phoneMetadata = MetadataManager.getShortNumberMetadataForRegion(region);
            if (phoneMetadata != null) {
                if (phoneMetadata.getGeneralDesc().getPossibleLengthList().contains(Integer.valueOf(shortNumberLength))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isValidShortNumberForRegion(PhoneNumber number, String regionDialingFrom) {
        if (!regionDialingFromMatchesNumber(number, regionDialingFrom)) {
            return false;
        }
        PhoneMetadata phoneMetadata = MetadataManager.getShortNumberMetadataForRegion(regionDialingFrom);
        if (phoneMetadata == null) {
            return false;
        }
        String shortNumber = getNationalSignificantNumber(number);
        if (matchesPossibleNumberAndNationalNumber(shortNumber, phoneMetadata.getGeneralDesc())) {
            return matchesPossibleNumberAndNationalNumber(shortNumber, phoneMetadata.getShortCode());
        }
        return false;
    }

    public boolean isValidShortNumber(PhoneNumber number) {
        List<String> regionCodes = getRegionCodesForCountryCode(number.getCountryCode());
        String regionCode = getRegionCodeForShortNumberFromRegionList(number, regionCodes);
        if (regionCodes.size() <= 1 || regionCode == null) {
            return isValidShortNumberForRegion(number, regionCode);
        }
        return true;
    }

    public ShortNumberCost getExpectedCostForRegion(PhoneNumber number, String regionDialingFrom) {
        if (!regionDialingFromMatchesNumber(number, regionDialingFrom)) {
            return ShortNumberCost.UNKNOWN_COST;
        }
        PhoneMetadata phoneMetadata = MetadataManager.getShortNumberMetadataForRegion(regionDialingFrom);
        if (phoneMetadata == null) {
            return ShortNumberCost.UNKNOWN_COST;
        }
        String shortNumber = getNationalSignificantNumber(number);
        if (!phoneMetadata.getGeneralDesc().getPossibleLengthList().contains(Integer.valueOf(shortNumber.length()))) {
            return ShortNumberCost.UNKNOWN_COST;
        }
        if (matchesPossibleNumberAndNationalNumber(shortNumber, phoneMetadata.getPremiumRate())) {
            return ShortNumberCost.PREMIUM_RATE;
        }
        if (matchesPossibleNumberAndNationalNumber(shortNumber, phoneMetadata.getStandardRate())) {
            return ShortNumberCost.STANDARD_RATE;
        }
        if (matchesPossibleNumberAndNationalNumber(shortNumber, phoneMetadata.getTollFree())) {
            return ShortNumberCost.TOLL_FREE;
        }
        if (isEmergencyNumber(shortNumber, regionDialingFrom)) {
            return ShortNumberCost.TOLL_FREE;
        }
        return ShortNumberCost.UNKNOWN_COST;
    }

    public ShortNumberCost getExpectedCost(PhoneNumber number) {
        List<String> regionCodes = getRegionCodesForCountryCode(number.getCountryCode());
        if (regionCodes.size() == 0) {
            return ShortNumberCost.UNKNOWN_COST;
        }
        if (regionCodes.size() == 1) {
            return getExpectedCostForRegion(number, (String) regionCodes.get(0));
        }
        ShortNumberCost cost = ShortNumberCost.TOLL_FREE;
        for (String regionCode : regionCodes) {
            ShortNumberCost costForRegion = getExpectedCostForRegion(number, regionCode);
            switch (costForRegion) {
                case PREMIUM_RATE:
                    return ShortNumberCost.PREMIUM_RATE;
                case UNKNOWN_COST:
                    cost = ShortNumberCost.UNKNOWN_COST;
                    break;
                case STANDARD_RATE:
                    if (cost == ShortNumberCost.UNKNOWN_COST) {
                        break;
                    }
                    cost = ShortNumberCost.STANDARD_RATE;
                    break;
                case TOLL_FREE:
                    break;
                default:
                    Logger logger = logger;
                    Level level = Level.SEVERE;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Unrecognised cost for region: ");
                    stringBuilder.append(costForRegion);
                    logger.log(level, stringBuilder.toString());
                    break;
            }
        }
        return cost;
    }

    private String getRegionCodeForShortNumberFromRegionList(PhoneNumber number, List<String> regionCodes) {
        if (regionCodes.size() == 0) {
            return null;
        }
        if (regionCodes.size() == 1) {
            return (String) regionCodes.get(0);
        }
        String nationalNumber = getNationalSignificantNumber(number);
        for (String regionCode : regionCodes) {
            PhoneMetadata phoneMetadata = MetadataManager.getShortNumberMetadataForRegion(regionCode);
            if (phoneMetadata != null && matchesPossibleNumberAndNationalNumber(nationalNumber, phoneMetadata.getShortCode())) {
                return regionCode;
            }
        }
        return null;
    }

    Set<String> getSupportedRegions() {
        return MetadataManager.getSupportedShortNumberRegions();
    }

    String getExampleShortNumber(String regionCode) {
        PhoneMetadata phoneMetadata = MetadataManager.getShortNumberMetadataForRegion(regionCode);
        if (phoneMetadata == null) {
            return "";
        }
        PhoneNumberDesc desc = phoneMetadata.getShortCode();
        if (desc.hasExampleNumber()) {
            return desc.getExampleNumber();
        }
        return "";
    }

    String getExampleShortNumberForCost(String regionCode, ShortNumberCost cost) {
        PhoneMetadata phoneMetadata = MetadataManager.getShortNumberMetadataForRegion(regionCode);
        if (phoneMetadata == null) {
            return "";
        }
        PhoneNumberDesc desc = null;
        int i = AnonymousClass1.$SwitchMap$com$google$i18n$phonenumbers$ShortNumberInfo$ShortNumberCost[cost.ordinal()];
        if (i != 1) {
            switch (i) {
                case 3:
                    desc = phoneMetadata.getStandardRate();
                    break;
                case 4:
                    desc = phoneMetadata.getTollFree();
                    break;
            }
        }
        desc = phoneMetadata.getPremiumRate();
        if (desc == null || !desc.hasExampleNumber()) {
            return "";
        }
        return desc.getExampleNumber();
    }

    public boolean connectsToEmergencyNumber(String number, String regionCode) {
        return matchesEmergencyNumberHelper(number, regionCode, true);
    }

    public boolean isEmergencyNumber(CharSequence number, String regionCode) {
        return matchesEmergencyNumberHelper(number, regionCode, false);
    }

    private boolean matchesEmergencyNumberHelper(CharSequence number, String regionCode, boolean allowPrefixMatch) {
        CharSequence possibleNumber = PhoneNumberUtil.extractPossibleNumber(number);
        boolean allowPrefixMatchForRegion = false;
        if (PhoneNumberUtil.PLUS_CHARS_PATTERN.matcher(possibleNumber).lookingAt()) {
            return false;
        }
        PhoneMetadata metadata = MetadataManager.getShortNumberMetadataForRegion(regionCode);
        if (metadata == null || !metadata.hasEmergency()) {
            return false;
        }
        String normalizedNumber = PhoneNumberUtil.normalizeDigitsOnly(possibleNumber);
        if (allowPrefixMatch && !REGIONS_WHERE_EMERGENCY_NUMBERS_MUST_BE_EXACT.contains(regionCode)) {
            allowPrefixMatchForRegion = true;
        }
        return this.matcherApi.matchNationalNumber(normalizedNumber, metadata.getEmergency(), allowPrefixMatchForRegion);
    }

    public boolean isCarrierSpecific(PhoneNumber number) {
        String regionCode = getRegionCodeForShortNumberFromRegionList(number, getRegionCodesForCountryCode(number.getCountryCode()));
        String nationalNumber = getNationalSignificantNumber(number);
        PhoneMetadata phoneMetadata = MetadataManager.getShortNumberMetadataForRegion(regionCode);
        return phoneMetadata != null && matchesPossibleNumberAndNationalNumber(nationalNumber, phoneMetadata.getCarrierSpecific());
    }

    public boolean isCarrierSpecificForRegion(PhoneNumber number, String regionDialingFrom) {
        boolean z = false;
        if (!regionDialingFromMatchesNumber(number, regionDialingFrom)) {
            return false;
        }
        String nationalNumber = getNationalSignificantNumber(number);
        PhoneMetadata phoneMetadata = MetadataManager.getShortNumberMetadataForRegion(regionDialingFrom);
        if (phoneMetadata != null && matchesPossibleNumberAndNationalNumber(nationalNumber, phoneMetadata.getCarrierSpecific())) {
            z = true;
        }
        return z;
    }

    public boolean isSmsServiceForRegion(PhoneNumber number, String regionDialingFrom) {
        boolean z = false;
        if (!regionDialingFromMatchesNumber(number, regionDialingFrom)) {
            return false;
        }
        PhoneMetadata phoneMetadata = MetadataManager.getShortNumberMetadataForRegion(regionDialingFrom);
        if (phoneMetadata != null && matchesPossibleNumberAndNationalNumber(getNationalSignificantNumber(number), phoneMetadata.getSmsServices())) {
            z = true;
        }
        return z;
    }

    private static String getNationalSignificantNumber(PhoneNumber number) {
        StringBuilder nationalNumber = new StringBuilder();
        if (number.isItalianLeadingZero()) {
            char[] zeros = new char[number.getNumberOfLeadingZeros()];
            Arrays.fill(zeros, '0');
            nationalNumber.append(new String(zeros));
        }
        nationalNumber.append(number.getNationalNumber());
        return nationalNumber.toString();
    }

    private boolean matchesPossibleNumberAndNationalNumber(String number, PhoneNumberDesc numberDesc) {
        if (numberDesc.getPossibleLengthCount() <= 0 || numberDesc.getPossibleLengthList().contains(Integer.valueOf(number.length()))) {
            return this.matcherApi.matchNationalNumber(number, numberDesc, false);
        }
        return false;
    }
}
