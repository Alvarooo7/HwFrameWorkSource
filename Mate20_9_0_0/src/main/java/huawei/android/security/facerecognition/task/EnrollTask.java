package huawei.android.security.facerecognition.task;

import android.view.Surface;
import huawei.android.security.facerecognition.FaceCamera;
import huawei.android.security.facerecognition.FaceRecognizeManagerImpl.CallbackHolder;
import huawei.android.security.facerecognition.base.HwSecurityTaskBase;
import huawei.android.security.facerecognition.base.HwSecurityTaskBase.RetCallback;
import huawei.android.security.facerecognition.base.HwSecurityTaskThread;
import huawei.android.security.facerecognition.request.EnrollRequest;
import huawei.android.security.facerecognition.utils.LogUtil;
import java.util.List;

public class EnrollTask extends FaceRecognizeTask {
    public static final String TAG = EnrollTask.class.getSimpleName();
    private byte[] mAuthToken;
    private RetCallback mDoCancelCallback = new RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase child, int ret) {
            if (EnrollTask.this.mTaskRequest.isActiveCanceled()) {
                CallbackHolder.getInstance().onCallbackEvent((int) EnrollTask.this.mTaskRequest.getReqId(), 1, 2, 0);
            }
            FaceCamera.getInstance().close();
            EnrollTask.this.endWithResult(ret);
        }
    };
    private RetCallback mDoEnrollCallback = new RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase child, int ret) {
            if (2 == ret) {
                HwSecurityTaskThread.staticPushTask(new DoCancelEnrollTask(EnrollTask.this, EnrollTask.this.mDoCancelCallback, EnrollTask.this.mTaskRequest), 1);
            } else if (ret != 0 && 6 != ret) {
                LogUtil.w(EnrollTask.TAG, "unknown failed!!!");
                EnrollTask.this.endEnroll(ret, 0, 0, 1);
            } else if (child instanceof DoEnrollTask) {
                DoEnrollTask detailTask = (DoEnrollTask) child;
                EnrollTask.this.endEnroll(ret, detailTask.getFaceId(), detailTask.getUserId(), detailTask.getErrorCode());
            } else {
                LogUtil.w(EnrollTask.TAG, "child has to be DoEnrollTask");
                EnrollTask.this.endEnroll(ret, 0, 0, 1);
            }
        }
    };
    private int mFlags;
    private RetCallback mPrepareCameraCallback = new RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase child, int ret) {
            if (ret != 0 && ret != 2) {
                CallbackHolder.getInstance().onCallbackEvent((int) EnrollTask.this.mTaskRequest.getReqId(), 1, 1, 1);
                FaceCamera.getInstance().close();
                EnrollTask.this.endWithResult(ret);
            } else if (EnrollTask.this.mTaskRequest.isCanceled()) {
                CallbackHolder.getInstance().onCallbackEvent((int) EnrollTask.this.mTaskRequest.getReqId(), 1, 1, 2);
                CallbackHolder.getInstance().onCallbackEvent((int) EnrollTask.this.mTaskRequest.getReqId(), 1, 2, 0);
                FaceCamera.getInstance().close();
                EnrollTask.this.endWithResult(2);
            } else {
                HwSecurityTaskThread.staticPushTask(new DoEnrollTask(EnrollTask.this, EnrollTask.this.mDoEnrollCallback, EnrollTask.this.mTaskRequest, EnrollTask.this.mAuthToken, EnrollTask.this.mFlags), 1);
            }
        }
    };
    private List<Surface> mSurfaces;

    public EnrollTask(FaceRecognizeTask parent, RetCallback callback, EnrollRequest request) {
        super(parent, callback, request);
        this.mAuthToken = request.getAuthToken();
        this.mFlags = request.getFlags();
        this.mSurfaces = request.getSurfaces();
    }

    public int doAction() {
        LogUtil.i("", "start enroll task");
        HwSecurityTaskThread.staticPushTask(new PrepareCameraTask(this, this.mPrepareCameraCallback, this.mTaskRequest, this.mSurfaces), 1);
        return -1;
    }

    private void endEnroll(int ret, int faceId, int userId, int errorCode) {
        CallbackHolder.getInstance().onCallbackEvent((int) this.mTaskRequest.getReqId(), 1, 1, errorCode);
        if (this.mTaskRequest.isActiveCanceled()) {
            CallbackHolder.getInstance().onCallbackEvent((int) this.mTaskRequest.getReqId(), 1, 2, 0);
        }
        FaceCamera.getInstance().close();
        endWithResult(ret);
    }
}
