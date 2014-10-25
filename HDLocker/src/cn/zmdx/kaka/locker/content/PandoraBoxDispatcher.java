
package cn.zmdx.kaka.locker.content;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import cn.zmdx.kaka.locker.BuildConfig;
import cn.zmdx.kaka.locker.HDApplication;
import cn.zmdx.kaka.locker.content.BaiduDataManager.BaiduData;
import cn.zmdx.kaka.locker.content.ServerDataManager.ServerData;
import cn.zmdx.kaka.locker.content.ServerImageDataManager.ServerImageData;
import cn.zmdx.kaka.locker.database.BaiduDataModel;
import cn.zmdx.kaka.locker.database.ServerDataModel;
import cn.zmdx.kaka.locker.database.ServerImageDataModel;
import cn.zmdx.kaka.locker.policy.PandoraPolicy;
import cn.zmdx.kaka.locker.settings.config.PandoraConfig;
import cn.zmdx.kaka.locker.utils.BaseInfoHelper;
import cn.zmdx.kaka.locker.utils.HDBLOG;
import cn.zmdx.kaka.locker.utils.HDBNetworkState;
import cn.zmdx.kaka.locker.utils.HDBThreadUtils;

public class PandoraBoxDispatcher extends Handler {

    public static final int MSG_BAIDU_DATA_ARRIVED = 0;

    public static final int MSG_PULL_BAIDU_DATA = 1;

    public static final int MSG_LOAD_BAIDU_IMG = 2;

    public static final int MSG_SERVER_DATA_ARRIVED = 3;

    public static final int MSG_SERVER_IMAGE_DATA_ARRIVED = 4;

    public static final int MSG_PULL_SERVER_TEXT_DATA = 5;

    public static final int MSG_PULL_SERVER_IMAGE_JOKE = 6;// 拉取搞笑图片的json数据(不包含图片下载)

    public static final int MSG_LOAD_SERVER_IMAGE_NEWS = 7;// 下载新闻图片的消息

    public static final int MSG_LOAD_SERVER_IMAGE_JOKE = 9;// 下载搞笑图片的消息

    public static final int MSG_PULL_SERVER_IMAGE_NEWS = 8;// 拉取新闻图片的json数据(不包含图片下载)

    public static final int MSG_PULL_SERVER_GIF = 10;

    public static final int MSG_LOAD_SERVER_GIF = 11;

    public static final int MSG_PULL_ORIGINAL_DATA = 12;

    public static final int MSG_ORIGINAL_DATA_ARRIVED = 13;

    public static final int MSG_DOWNLOAD_IMAGES = 14;

    private static PandoraBoxDispatcher INSTANCE;

    private PandoraConfig mConfig;

    private PandoraBoxDispatcher(Looper looper) {
        super(looper);
        mConfig = PandoraConfig.newInstance(HDApplication.getInstannce());
    }

    public static synchronized PandoraBoxDispatcher getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PandoraBoxDispatcher(HDBThreadUtils.getWorkerLooper());
        }
        return INSTANCE;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_PULL_ORIGINAL_DATA:
                if (BuildConfig.DEBUG) {
                    HDBLOG.logD("收到拉取原始数据消息");
                }
                if (!HDBNetworkState.isNetworkAvailable()) {
                    if (BuildConfig.DEBUG) {
                        HDBLOG.logD("无网络，中断拉取原始数据");
                    }
                    return;
                }
                if (!checkOriginalDataPullable()) {
                    if (BuildConfig.DEBUG) {
                        HDBLOG.logD("拉取原始数据条件不满足，中断拉取");
                    }
                    return;
                }
                processPullOriginalData();
                break;
            case MSG_DOWNLOAD_IMAGES:
                if (BuildConfig.DEBUG) {
                    HDBLOG.logD("收到下载图片的消息");
                }
                loadPandoraServerImage();
                break;
            case MSG_BAIDU_DATA_ARRIVED:
                @SuppressWarnings("unchecked")
                final List<BaiduData> bdList = (List<BaiduData>) msg.obj;
                BaiduData.saveToDatabase(bdList);
                if (BuildConfig.DEBUG) {
                    HDBLOG.logD("收到百度图片的数据，准备入库，条数:" + bdList.size());
                }
                break;
            case MSG_PULL_BAIDU_DATA:
                if (BuildConfig.DEBUG) {
                    HDBLOG.logD("收到抓取百度数据的消息");
                }
                if (checkBaiduDataPullable()) {
                    if (BuildConfig.DEBUG) {
                        HDBLOG.logD("满足拉取百度图片的条件，开始拉取...");
                    }
                    processPullBaiduData();
                    mConfig.saveLastPullBaiduTime(System.currentTimeMillis());
                } else {
                    if (BuildConfig.DEBUG) {
                        HDBLOG.logD("不满足拉取百度图片的条件，停止拉取...");
                    }
                }
                break;
            case MSG_PULL_SERVER_TEXT_DATA:
                if (BuildConfig.DEBUG) {
                    HDBLOG.logD("收到拉取文本数据的消息");
                }
                processPullServerTextData();
                break;
            case MSG_PULL_SERVER_IMAGE_JOKE:
                if (BuildConfig.DEBUG) {
                    HDBLOG.logD("收到拉取PANDORA搞笑图片数据的消息");
                }
                processPullServerImageData(ServerDataMapping.S_DATATYPE_JOKE);
                break;
            case MSG_PULL_SERVER_IMAGE_NEWS:
                if (BuildConfig.DEBUG) {
                    HDBLOG.logD("收到拉取PANDORA NEWS图片数据的消息");
                }
                processPullServerImageData(ServerDataMapping.S_DATATYPE_NEWS);
                break;
            case MSG_PULL_SERVER_GIF:
                if (BuildConfig.DEBUG) {
                    HDBLOG.logD("收到拉取PANDORA GIF图片数据的消息");
                }
                processPullServerImageData(ServerDataMapping.S_DATATYPE_GIF);
                break;
            case MSG_LOAD_BAIDU_IMG:
                if (BuildConfig.DEBUG) {
                    HDBLOG.logD("收到下载百度图片的消息");
                }
                loadBaiduImage();
                break;
            case MSG_LOAD_SERVER_IMAGE_NEWS:
                if (BuildConfig.DEBUG) {
                    HDBLOG.logD("收到下载阿里云新闻图片的消息");
                }
                // loadPandoraServerImage(ServerDataMapping.S_DATATYPE_NEWS);
                break;
            case MSG_LOAD_SERVER_IMAGE_JOKE:
                if (BuildConfig.DEBUG) {
                    HDBLOG.logD("收到下载阿里云搞笑图片的消息");
                }
                // loadPandoraServerImage(ServerDataMapping.S_DATATYPE_JOKE);
                break;
            case MSG_LOAD_SERVER_GIF:
                if (BuildConfig.DEBUG) {
                    HDBLOG.logD("收到下载阿里云GIF图片的消息");
                }
                // loadPandoraServerImage(ServerDataMapping.S_DATATYPE_GIF);
                break;
            case MSG_SERVER_DATA_ARRIVED:
                if (BuildConfig.DEBUG) {
                    HDBLOG.logD("server文本数据已经下载完成，准备入库");
                }
                @SuppressWarnings("unchecked")
                final List<ServerData> sdList = (List<ServerData>) msg.obj;
                ServerData.saveToDatabase(sdList);
                break;
            case MSG_SERVER_IMAGE_DATA_ARRIVED:
                @SuppressWarnings("unchecked")
                final List<ServerImageData> sidList = (List<ServerImageData>) msg.obj;
                if (BuildConfig.DEBUG) {
                    HDBLOG.logD("server Image数据已经下载完成，开始入库，条数:" + sidList.size());
                }
                ServerImageData.saveToDatabase(sidList);
                break;
            case MSG_ORIGINAL_DATA_ARRIVED:
                @SuppressWarnings("unchecked")
                final List<ServerImageData> oriDataList = (List<ServerImageData>) msg.obj;
                if (BuildConfig.DEBUG) {
                    HDBLOG.logD("原始数据已经下载完成，开始入库，条数:" + oriDataList.size());
                }
                //标记最后一次拉取原始数据的时间
                mConfig.saveLastPullOriginalDataTime(System.currentTimeMillis());

                //如果这是今天第一次拉取，则删除当前库中的旧数据
                if (checkFirstPullToday()) {
                    ServerImageDataModel.getInstance().deleteAll();
                    DiskImageHelper.clear();
                }
                //标记今天已经拉取过原始数据
                mConfig.saveTodayPullOriginalDataTime(BaseInfoHelper.getCurrentDate());

                if (oriDataList.size() <= 0) {
                    return;
                }
                //将今天的数据保存到本地数据库
                ServerImageData.saveToDatabase(oriDataList);
                break;
        }

        super.handleMessage(msg);
    }

    private boolean checkFirstPullToday() {
        String date = mConfig.getTodayPullOriginalData();
        String currentDate = BaseInfoHelper.getCurrentDate();
        return !date.equals(currentDate);
    }

    private boolean checkOriginalDataPullable() {
        long lastTime = mConfig.getLastTimePullOriginalData();
        return System.currentTimeMillis() - lastTime > PandoraPolicy.MIN_PULL_ORIGINAL_TIME;
    }

    /**
     * 检查是否满足拉取百度图片的条件
     * 
     * @return
     */
    private boolean checkBaiduDataPullable() {
        final long lastPullTime = mConfig.getLastPullBaiduTime();
        return System.currentTimeMillis() - lastPullTime > PandoraPolicy.PULL_BAIDU_INTERVAL_TIME;
    }

    private void loadPandoraServerImage() {
        if (HDBNetworkState.isNetworkAvailable()) {
            // 如果磁盘缓存区图片数为0，则更新数据库中的是否下载字段为否
            if (DiskImageHelper.getFileCountOnDisk() <= 1) {
                if (BuildConfig.DEBUG) {
                    HDBLOG.logD("图片的本地磁盘存储的数量为0，清空数据库中的已下载标记，并开启下载图片程序");
                }
                ServerImageDataModel.getInstance().markAllNonDownload();
                downloadServerImages();
                return;
            }

            int hasImageCount = ServerImageDataModel.getInstance().queryCountHasImage(null);
            if (hasImageCount < PandoraPolicy.MIN_COUNT_LOCAL_DB_HAS_IMAGE) {
                if (BuildConfig.DEBUG) {
                    HDBLOG.logD("ServerImage数据库中标记为已下载的数据总数:" + hasImageCount + "已小于阀值:"
                            + PandoraPolicy.MIN_COUNT_LOCAL_DB_HAS_IMAGE + ",立即开启下载图片程序");
                }
                downloadServerImages();
            } else {
                if (BuildConfig.DEBUG) {
                    HDBLOG.logD("ServerImage数据库中标记为已下载的数据总数为:" + hasImageCount + "大于最小阀值"
                            + PandoraPolicy.MIN_COUNT_LOCAL_DB_HAS_IMAGE + ",无需启动下载图片程序");
                }
            }
        }
    }

    private void downloadServerImages() {
        // 根据不同网络情况查询出不同数量的数据，准备下载其图片
        // 规则说明：若wifi,则每个频道取5条数据，共5*5=25条数据；若非wifi，则每个频道取1条，共1 * 5 = 5条数据
        int count = HDBNetworkState.isWifiNetwork() ? PandoraPolicy.COUNT_DOWNLOAD_IMAGE_WIFI
                : PandoraPolicy.COUNT_DOWNLOAD_IMAGE_NON_WIFI;
        List<ServerImageData> list = new ArrayList<ServerImageData>();
        List<ServerImageData> tmpList = ServerImageDataModel.getInstance().queryWithoutImg(count);
        list.addAll(tmpList);
        ServerImageDataManager.getInstance().batchDownloadServerImage(list);
    }

    private void loadBaiduImage() {
        if (HDBNetworkState.isNetworkAvailable()) {
            // 如果磁盘缓存区图片数为0，则更新数据库中的是否下载字段为否
            if (DiskImageHelper.getFileCountOnDisk() <= 1) {
                if (BuildConfig.DEBUG) {
                    HDBLOG.logD("百度图片的本地磁盘存储的数量为0，清空数据库中的已下载标记，并开启下载图片程序");
                }
                BaiduDataModel.getInstance().markAllNonDownload();
                downloadBaiduPartImages();
                return;
            }

            int hasImageCount = BaiduDataModel.getInstance().queryCountHasImage();
            if (hasImageCount < PandoraPolicy.MIN_COUNT_LOCAL_DB_HAS_IMAGE) {
                if (BuildConfig.DEBUG) {
                    HDBLOG.logD("baidu数据库中标记为已下载的数据总数:" + hasImageCount + "已小于阀值:"
                            + PandoraPolicy.MIN_COUNT_LOCAL_DB_HAS_IMAGE + ",立即开启下载图片程序");
                }
                downloadBaiduPartImages();
            } else {
                if (BuildConfig.DEBUG) {
                    HDBLOG.logD("baidu数据库中标记为已下载的数据总数为:" + hasImageCount + "大于最小阀值"
                            + PandoraPolicy.MIN_COUNT_LOCAL_DB_HAS_IMAGE + ",无需启动下载图片程序");
                }
            }
        }
    }

    private void processPullOriginalData() {
        boolean isFirstPull = checkFirstPullToday();
        long lastModified = 0;
        if (!isFirstPull) {
            lastModified = ServerImageDataModel.getInstance().queryLastModifiedOfToday();
        }
        ServerImageDataManager.getInstance().pullTodayData(lastModified);
    }

    private void processPullBaiduData() {
        int totalCount = BaiduDataModel.getInstance().queryTotalCount();
        // 如果本地的百度图片库中的数据条数已经少于一定的值，则启动拉取程序
        if (totalCount < PandoraPolicy.MIN_COUNT_LOCAL_DB) {
            if (HDBNetworkState.isNetworkAvailable()) {
                if (BuildConfig.DEBUG) {
                    HDBLOG.logD("baidu本地数据库的数据还有" + totalCount + "条数据，少于最小阀值，马上启动抓取程序");
                }

                BaiduDataManager.getInstance().pullAllFunnyData();
            } else {
                if (BuildConfig.DEBUG) {
                    HDBLOG.logD("当前无网络，停止启动抓取程序");
                }
            }
        } else {
            if (BuildConfig.DEBUG) {
                HDBLOG.logD("baidu本地数据库的数据还有" + totalCount + "条数据，无需启动抓取程序");
            }
        }
    }

    private void processPullServerTextData() {
        if (!HDBNetworkState.isNetworkAvailable()) {
            if (BuildConfig.DEBUG) {
                HDBLOG.logD("无可用网络，停止拉取");
            }
            return;
        }

        int totalCount = ServerDataModel.getInstance().queryTotalCount();
        if (totalCount < PandoraPolicy.MIN_COUNT_LOCAL_DB) {
            ServerDataManager.getInstance().pullServerData(40, ServerDataMapping.S_DATATYPE_JOKE,
                    ServerDataMapping.S_WEBSITE_ALL);
            if (BuildConfig.DEBUG) {
                HDBLOG.logD("满足拉取文本数据条件，开始拉取ServerData");
            }
        } else {
            if (BuildConfig.DEBUG) {
                HDBLOG.logD("不满足拉取文本数据条件，无需拉取ServerData");
            }
        }
    }

    private void processPullServerImageData(String dataType) {
        if (!HDBNetworkState.isNetworkAvailable()) {
            if (BuildConfig.DEBUG) {
                HDBLOG.logD("无可用网络，停止拉取");
            }
            return;
        }
        int totalCount = ServerImageDataModel.getInstance().queryCountByType(dataType);
        if (totalCount < PandoraPolicy.MIN_COUNT_PANDORA_IMAGE) {
            ServerImageDataManager.getInstance().pullServerImageData(60, dataType,
                    ServerDataMapping.S_WEBSITE_ALL);
            if (BuildConfig.DEBUG) {
                HDBLOG.logD("满足拉取pandora图片数据条件，开始拉取ServerImageData,当前数据类型为：" + dataType + ",本地数量为:"
                        + totalCount);
            }
        } else {
            if (BuildConfig.DEBUG) {
                HDBLOG.logD("不满足拉取pandora图片数据条件，无需拉取ServerImageData,当前数据类型为：" + dataType
                        + ",本地数量为:" + totalCount);
            }
        }
    }

    private void downloadBaiduPartImages() {
        // 根据不同网络情况查询出不同数量的数据，准备下载其图片
        // 规则说明：若wifi,则每个频道取5条数据，共5*5=25条数据；若非wifi，则每个频道取1条，共1 * 5 = 5条数据
        int count = HDBNetworkState.isWifiNetwork() ? PandoraPolicy.COUNT_DOWNLOAD_IMAGE_WIFI
                : PandoraPolicy.COUNT_DOWNLOAD_IMAGE_NON_WIFI;
        List<BaiduData> list = new ArrayList<BaiduData>();
        int length = PandoraPolicy.BAIDU_IMAGE_MODULE.length;
        for (int i = 0; i < length; i++) {
            list.addAll(BaiduDataModel.getInstance().queryNonImageData(
                    PandoraPolicy.BAIDU_IMAGE_MODULE[i], count));
        }
        BaiduDataManager.getInstance().batchDownloadBaiduImage(list);
    }
}
