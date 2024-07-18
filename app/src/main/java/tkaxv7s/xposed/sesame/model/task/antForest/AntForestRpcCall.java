package tkaxv7s.xposed.sesame.model.task.antForest;

import tkaxv7s.xposed.sesame.entity.AlipayVersion;
import tkaxv7s.xposed.sesame.entity.RpcEntity;
import tkaxv7s.xposed.sesame.hook.ApplicationHook;
import tkaxv7s.xposed.sesame.util.RandomUtil;
import tkaxv7s.xposed.sesame.util.StringUtil;

import java.util.List;
import java.util.UUID;

public class AntForestRpcCall {

    private static String VERSION = "";

    public static void init() {
        AlipayVersion alipayVersion = ApplicationHook.getAlipayVersion();
        if (alipayVersion.compareTo(new AlipayVersion("10.5.88.8000")) > 0) {
            VERSION = "20240403";
        } else if (alipayVersion.compareTo(new AlipayVersion("10.3.96.8100")) > 0) {
            VERSION = "20230501";
        } else {
            VERSION = "20230501";
        }
    }

    private static String getUniqueId() {
        return String.valueOf(System.currentTimeMillis()) + RandomUtil.nextLong();
    }

    public static String queryEnergyRanking() {
        return ApplicationHook.requestString("alipay.antmember.forest.h5.queryEnergyRanking",
                "[{\"periodType\":\"total\",\"rankType\":\"energyRank\",\"source\":\"chInfo_ch_appcenter__chsub_9patch\",\"version\":\""
                        + VERSION + "\"}]", "{\"pathList\":[\"friendRanking\",\"myself\",\"totalDatas\"]}");
    }

    public static String fillUserRobFlag(String userIdList) {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.fillUserRobFlag",
                "[{\"userIdList\":" + userIdList + "}]", "{\"pathList\":[\"friendRanking\"]}");
    }

    public static String queryHomePage() {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.queryHomePage",
                "[{\"configVersionMap\":{\"wateringBubbleConfig\":\"10\"},\"skipWhackMole\":true,\"source\":\"chInfo_ch_appcenter__chsub_9patch\",\"version\":\""
                        + VERSION + "\"}]", 3, 1000);
    }

    public static String queryFriendHomePage(String userId) {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.queryFriendHomePage",
                "[{\"canRobFlags\":\"F,F,F,F,F\",\"configVersionMap\":{\"redPacketConfig\":0,\"wateringBubbleConfig\":\"10\"},\"source\":\"chInfo_ch_appcenter__chsub_9patch\",\"userId\":\""
                        + userId + "\",\"version\":\"" + VERSION + "\"}]", 3, 1000);
    }

    public static RpcEntity getCollectEnergyRpcEntity(String bizType, String userId, long bubbleId) {
        String args1;
        if (StringUtil.isEmpty(bizType)) {
            args1 = "[{\"bizType\":\"\",\"bubbleIds\":[" + bubbleId
                    + "],\"source\":\"chInfo_ch_appcenter__chsub_9patch\",\"userId\":\"" + userId + "\",\"version\":\""
                    + VERSION + "\"}]";
        } else {
            args1 = "[{\"bizType\":\"" + bizType + "\",\"bubbleIds\":[" + bubbleId
                    + "],\"source\":\"chInfo_ch_appcenter__chsub_9patch\",\"userId\":\"" + userId + "\"}]";
        }
        return new RpcEntity("alipay.antmember.forest.h5.collectEnergy", args1, null);
    }

    public static String collectEnergy(String bizType, String userId, Long bubbleId) {
        return ApplicationHook.requestString(getCollectEnergyRpcEntity(bizType, userId, bubbleId));
    }

    public static RpcEntity getCollectBatchEnergyRpcEntity(String userId, List<Long> bubbleIdList) {
        return getCollectBatchEnergyRpcEntity(userId, StringUtil.collectionJoinString(",", bubbleIdList));
    }

    public static RpcEntity getCollectBatchEnergyRpcEntity(String userId, String bubbleIds) {
        return new RpcEntity("alipay.antmember.forest.h5.collectEnergy", "[{\"bizType\":\"\",\"bubbleIds\":[" + bubbleIds
                + "],\"fromAct\":\"BATCH_ROB_ENERGY\",\"source\":\"chInfo_ch_appcenter__chsub_9patch\",\"userId\":\"" + userId + "\",\"version\":\""
                + VERSION + "\"}]");
    }

    public static String collectBatchEnergy(String userId, List<Long> bubbleId) {
        return ApplicationHook.requestString(getCollectBatchEnergyRpcEntity(userId, bubbleId));
    }

    public static String collectRebornEnergy() {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.collectRebornEnergy", "[{\"source\":\"chInfo_ch_appcenter__chsub_9patch\"}]");
    }

    public static String transferEnergy(String targetUser, String bizNo, int energyId) {
        return ApplicationHook.requestString("alipay.antmember.forest.h5.transferEnergy", "[{\"bizNo\":\"" +
                bizNo + UUID.randomUUID().toString() + "\",\"energyId\":" + energyId +
                ",\"extInfo\":{\"sendChat\":\"N\"},\"from\":\"friendIndex\",\"source\":\"chInfo_ch_appcenter__chsub_9patch\",\"targetUser\":\""
                + targetUser + "\",\"transferType\":\"WATERING\",\"version\":\"" + VERSION + "\"}]");
    }

    public static String forFriendCollectEnergy(String targetUserId, long bubbleId) {
        String args1 = "[{\"bubbleIds\":[" + bubbleId + "],\"targetUserId\":\"" + targetUserId + "\"}]";
        return ApplicationHook.requestString("alipay.antmember.forest.h5.forFriendCollectEnergy", args1);
    }

    public static String vitalitySign() {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.vitalitySign",
                "[{\"source\":\"chInfo_ch_appcenter__chsub_9patch\"}]");
    }

    public static String queryTaskList() {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.queryTaskList",
                "[{\"extend\":{},\"fromAct\":\"home_task_list\",\"source\":\"chInfo_ch_appcenter__chsub_9patch\",\"version\":\""
                        + VERSION + "\"}]");
    }

    public static String queryEnergyRainHome() {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.queryEnergyRainHome", "[{\"version\":\"" + VERSION + "\"}]");
    }

    public static String queryEnergyRainCanGrantList() {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.queryEnergyRainCanGrantList", "[{}]");
    }

    public static String grantEnergyRainChance(String targetUserId) {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.grantEnergyRainChance",
                "[{\"targetUserId\":" + targetUserId + "}]");
    }

    public static String startEnergyRain() {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.startEnergyRain", "[{\"version\":\"" + VERSION + "\"}]");
    }

    public static String energyRainSettlement(int saveEnergy, String token) {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.energyRainSettlement",
                "[{\"activityPropNums\":0,\"saveEnergy\":" + saveEnergy + ",\"token\":\"" + token + "\",\"version\":\""
                        + VERSION + "\"}]");
    }

    public static String receiveTaskAward(String sceneCode, String taskType) {
        return ApplicationHook.requestString("com.alipay.antiep.receiveTaskAward",
                "[{\"ignoreLimit\":false,\"requestType\":\"H5\",\"sceneCode\":\"" + sceneCode +
                        "\",\"source\":\"ANTFOREST\",\"taskType\":\"" + taskType + "\"}]");
    }

    public static String finishTask(String sceneCode, String taskType) {
        String outBizNo = taskType + "_" + RandomUtil.nextDouble();
        return ApplicationHook.requestString("com.alipay.antiep.finishTask",
                "[{\"outBizNo\":\"" + outBizNo + "\",\"requestType\":\"H5\",\"sceneCode\":\"" +
                        sceneCode + "\",\"source\":\"ANTFOREST\",\"taskType\":\"" + taskType + "\"}]");
    }

    public static String popupTask() {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.popupTask",
                "[{\"fromAct\":\"pop_task\",\"needInitSign\":false,\"source\":\"chInfo_ch_appcenter__chsub_9patch\",\"statusList\":[\"TODO\",\"FINISHED\"],\"version\":\""
                        + VERSION + "\"}]");
    }

    public static String antiepSign(String entityId, String userId) {
        return ApplicationHook.requestString("com.alipay.antiep.sign",
                "[{\"entityId\":\"" + entityId
                        + "\",\"requestType\":\"rpc\",\"sceneCode\":\"ANTFOREST_ENERGY_SIGN\",\"source\":\"ANTFOREST\",\"userId\":\""
                        + userId + "\"}]");
    }

    public static String queryPropList(boolean onlyGive) {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.queryPropList",
                "[{\"onlyGive\":\"" + (onlyGive ? "Y" : "")
                        + "\",\"source\":\"chInfo_ch_appcenter__chsub_9patch\",\"version\":\"" + VERSION + "\"}]");
    }

    // 查询可派遣伙伴
    public static String queryAnimalPropList() {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.queryAnimalPropList",
                "[{\"source\":\"chInfo_ch_appcenter__chsub_9patch\"}]");
    }

    // 派遣动物伙伴
    public static String consumeProp(String propGroup, String propType, Boolean replace) {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.consumeProp",
                "[{\"propGroup\":\"" + propGroup
                        + "\",\"propType\":\"" + propType
                        + "\",\"replace\":\"" + (replace ? "true" : "false")
                        + "\",\"sToken\":\"" + System.currentTimeMillis()
                        + "\",\"source\":\"chInfo_ch_appcenter__chsub_9patch\"}]");
    }

    public static String giveProp(String giveConfigId, String propId, String targetUserId) {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.giveProp",
                "[{\"giveConfigId\":\"" + giveConfigId + "\",\"propId\":\"" + propId
                        + "\",\"source\":\"self_corner\",\"targetUserId\":\"" + targetUserId + "\"}]");
    }

    public static String collectProp(String giveConfigId, String giveId) {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.collectProp",
                "[{\"giveConfigId\":\"" + giveConfigId + "\",\"giveId\":\"" + giveId
                        + "\",\"source\":\"chInfo_ch_appcenter__chsub_9patch\"}]");
    }

    public static String consumeProp(String propId, String propType) {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.consumeProp",
                "[{\"propId\":\"" + propId + "\",\"propType\":\"" + propType +
                        "\",\"source\":\"chInfo_ch_appcenter__chsub_9patch\",\"timezoneId\":\"Asia/Shanghai\",\"version\":\""
                        + VERSION + "\"}]");
    }

    public static String itemList(String labelType) {
        return ApplicationHook.requestString("com.alipay.antiep.itemList",
                "[{\"extendInfo\":\"{}\",\"labelType\":\"" + labelType
                        + "\",\"pageSize\":20,\"requestType\":\"rpc\",\"sceneCode\":\"ANTFOREST_VITALITY\",\"source\":\"afEntry\",\"startIndex\":0}]");
    }

    public static String itemDetail(String spuId) {
        return ApplicationHook.requestString("com.alipay.antiep.itemDetail",
                "[{\"requestType\":\"rpc\",\"sceneCode\":\"ANTFOREST_VITALITY\",\"source\":\"afEntry\",\"spuId\":\""
                        + spuId + "\"}]");
    }

    public static String queryVitalityStoreIndex() {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.queryVitalityStoreIndex",
                "[{\"source\":\"afEntry\"}]");
    }

    public static String exchangeBenefit(String spuId, String skuId) {
        return ApplicationHook.requestString("com.alipay.antcommonweal.exchange.h5.exchangeBenefit",
                "[{\"sceneCode\":\"ANTFOREST_VITALITY\",\"requestId\":\"" + System.currentTimeMillis()
                        + "_" + RandomUtil.getRandom(17) + "\",\"spuId\":\"" +
                        spuId + "\",\"skuId\":\"" + skuId + "\",\"source\":\"GOOD_DETAIL\"}]");
    }

    public static String testH5Rpc(String operationTpye, String requestDate) {
        return ApplicationHook.requestString(operationTpye, requestDate);
    }

    /* 巡护保护地 */
    public static String queryUserPatrol() {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.queryUserPatrol",
                "[{\"source\":\"ant_forest\",\"timezoneId\":\"Asia/Shanghai\"}]");
    }

    public static String queryMyPatrolRecord() {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.queryMyPatrolRecord",
                "[{\"source\":\"ant_forest\",\"timezoneId\":\"Asia/Shanghai\"}]");
    }

    public static String switchUserPatrol(String targetPatrolId) {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.switchUserPatrol",
                "[{\"source\":\"ant_forest\",\"targetPatrolId\":" + targetPatrolId + ",\"timezoneId\":\"Asia/Shanghai\"}]");
    }

    public static String patrolGo(int nodeIndex, int patrolId) {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.patrolGo",
                "[{\"nodeIndex\":" + nodeIndex + ",\"patrolId\":" + patrolId
                        + ",\"source\":\"ant_forest\",\"timezoneId\":\"Asia/Shanghai\"}]");
    }

    public static String patrolKeepGoing(int nodeIndex, int patrolId, String eventType) {
        String args = null;
        switch (eventType) {
            case "video":
                args = "[{\"nodeIndex\":" + nodeIndex + ",\"patrolId\":" + patrolId
                        + ",\"reactParam\":{\"viewed\":\"Y\"},\"source\":\"ant_forest\",\"timezoneId\":\"Asia/Shanghai\"}]";
                break;
            case "chase":
                args = "[{\"nodeIndex\":" + nodeIndex + ",\"patrolId\":" + patrolId
                        + ",\"reactParam\":{\"sendChat\":\"Y\"},\"source\":\"ant_forest\",\"timezoneId\":\"Asia/Shanghai\"}]";
                break;
            case "quiz":
                args = "[{\"nodeIndex\":" + nodeIndex + ",\"patrolId\":" + patrolId
                        + ",\"reactParam\":{\"answer\":\"correct\"},\"source\":\"ant_forest\",\"timezoneId\":\"Asia/Shanghai\"}]";
                break;
            default:
                args = "[{\"nodeIndex\":" + nodeIndex + ",\"patrolId\":" + patrolId
                        + ",\"reactParam\":{},\"source\":\"ant_forest\",\"timezoneId\":\"Asia/Shanghai\"}]";
                break;
        }
        return ApplicationHook.requestString("alipay.antforest.forest.h5.patrolKeepGoing", args);
    }

    public static String exchangePatrolChance(int costStep) {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.exchangePatrolChance",
                "[{\"costStep\":" + costStep + ",\"source\":\"ant_forest\",\"timezoneId\":\"Asia/Shanghai\"}]");
    }

    public static String queryAnimalAndPiece(int animalId) {
        String args = null;
        if (animalId != 0) {
            args = "[{\"animalId\":" + animalId + ",\"source\":\"ant_forest\",\"timezoneId\":\"Asia/Shanghai\"}]";
        } else {
            args = "[{\"source\":\"ant_forest\",\"timezoneId\":\"Asia/Shanghai\",\"withDetail\":\"N\",\"withGift\":true}]";
        }
        return ApplicationHook.requestString("alipay.antforest.forest.h5.queryAnimalAndPiece", args);
    }

    public static String combineAnimalPiece(int animalId, String piecePropIds) {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.combineAnimalPiece",
                "[{\"animalId\":" + animalId + ",\"piecePropIds\":" + piecePropIds
                        + ",\"timezoneId\":\"Asia/Shanghai\",\"source\":\"ant_forest\"}]");
    }

    public static String AnimalConsumeProp(String propGroup, String propId, String propType) {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.consumeProp",
                "[{\"propGroup\":\"" + propGroup + "\",\"propId\":\"" + propId + "\",\"propType\":\"" + propType
                        + "\",\"source\":\"ant_forest\",\"timezoneId\":\"Asia/Shanghai\"}]");
    }

    public static String collectAnimalRobEnergy(String propId, String propType, String shortDay) {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.collectAnimalRobEnergy",
                "[{\"propId\":\"" + propId + "\",\"propType\":\"" + propType + "\",\"shortDay\":\"" + shortDay
                        + "\",\"source\":\"chInfo_ch_appcenter__chsub_9patch\"}]");
    }

    /* 复活能量 */
    public static String protectBubble(String targetUserId) {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.protectBubble",
                "[{\"source\":\"ANT_FOREST_H5\",\"targetUserId\":\"" + targetUserId + "\",\"version\":\"" + VERSION
                        + "\"}]");
    }

    /* 森林礼盒 */
    public static String collectFriendGiftBox(String targetId, String targetUserId) {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.collectFriendGiftBox",
                "[{\"source\":\"chInfo_ch_appcenter__chsub_9patch\",\"targetId\":\"" + targetId
                        + "\",\"targetUserId\":\"" + targetUserId + "\"}]");
    }

    /* 6秒拼手速 打地鼠 */
    public static String startWhackMole() {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.startWhackMole",
                "[{\"source\":\"chInfo_ch_appcenter__chsub_9patch\"}]");
    }

    public static String settlementWhackMole(String token, List<String> moleIdList) {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.settlementWhackMole",
                "[{\"moleIdList\":[" + String.join(",", moleIdList)
                        + "],\"settlementScene\":\"NORMAL\",\"source\":\"chInfo_ch_appcenter__chsub_9patch\",\"token\":\"" + token + "\",\"version\":\"" + VERSION + "\"}]");
    }

    public static String closeWhackMole() {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.updateUserConfig", "[{\"configMap\":{\"whackMole\":\"N\"},\"source\":\"chInfo_ch_appcenter__chsub_9patch\"}]");
    }

    /* 森林集市 */
    public static String consultForSendEnergyByAction(String sourceType) {
        return ApplicationHook.requestString("alipay.bizfmcg.greenlife.consultForSendEnergyByAction",
                "[{\"sourceType\":\"" + sourceType + "\"}]");
    }

    public static String sendEnergyByAction(String sourceType) {
        return ApplicationHook.requestString("alipay.bizfmcg.greenlife.sendEnergyByAction",
                "[{\"actionType\":\"GOODS_BROWSE\",\"requestId\":\"" + RandomUtil.getRandomString(8) + "\",\"sourceType\":\"" + sourceType + "\"}]");
    }

    /* 翻倍额外能量收取 */
    public static String collectRobExpandEnergy(String propId, String propType) {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.collectRobExpandEnergy",
                "[{\"propId\":\"" + propId + "\",\"propType\":\"" + propType
                        + "\",\"source\":\"chInfo_ch_appcenter__chsub_9patch\"}]");
    }

    /* 医疗健康 */
    public static String medical_health_feeds_query() {
        return ApplicationHook.requestString("alipay.iblib.channel.build.query",
                "[{\"activityCode\":\"medical_health_feeds_query\",\"activityId\":\"2023072600001207\",\"body\":{\"apiVersion\":\"3.1.0\",\"bizId\":\"B213\",\"businessCode\":\"JKhealth\",\"businessId\":\"O2023071900061804\",\"cityCode\":\"330100\",\"cityName\":\"杭州\",\"exclContentIds\":[],\"filterItems\":[],\"latitude\":\"\",\"longitude\":\"\",\"moduleParam\":{\"COMMON_FEEDS_BLOCK_2024041200243259\":{}},\"pageCode\":\"YM2024041200137150\",\"pageNo\":1,\"pageSize\":10,\"pid\":\"BC_PD_20230713000008526\",\"queryQuizActivityFeed\":1,\"scenceCode\":\"HEALTH_CHANNEL\",\"schemeParams\":{},\"scope\":\"PARTIAL\",\"selectedTabCode\":\"\",\"sourceType\":\"miniApp\",\"specialItemId\":\"\",\"specialItemType\":\"\",\"tenantCode\":\"2021003141652419\",\"underTakeContentId\":\"\"},\"version\":\"2.0\"}]");
    }

    /*
     * public static String medical_health_feeds_query() {
     * return ApplicationHook.requestString("alipay.iblib.channel.build.query",
     * "[{\"activityCode\":\"medical_health_feeds_query\",\"activityId\":\"2023072600001207\",\"body\":{\"apiVersion\":\"3.1.0\",\"bizId\":\"B213\",\"businessCode\":\"JKhealth\",\"businessId\":\"O2023071900061804\",\"cityCode\":\"330100\",\"cityName\":\"杭州\",\"exclContentIds\":[\"20240611OB020010036515121805\",\"20240618OB020010036519694606\",\"20240531OB020010039908594289\",\"20240618OB020010031219943466\",\"20240130OB020010034821821452\",\"20240531OB020010039908610960\",\"20240520OB020010035100844933\",\"20230926OB020010033829802408\",\"20240612OB020010039916083635\",\"20240510OB020010031294655966\",\"20240520OB020010030300850750\",\"20230928OB020010030332233578\",\"20220519OB020010035308350001\",\"20240104OB020010032288343993\",\"20220517OB020010038708340106\",\"20240606OB020010039912316758\",\"20240529OB020010033806968404\",\"20240614OB020010039917386188\",\"20230830OB020010039920939091\",\"20231124OB020010036561478030\"],\"filterItems\":[],\"latitude\":\"\",\"longitude\":\"\",\"moduleParam\":{\"COMMON_FEEDS_BLOCK_2024041200243259\":{}},\"pageCode\":\"YM2024041200137150\",\"pageNo\":1,\"pageSize\":10,\"pid\":\"BC_PD_20230713000008526\",\"queryQuizActivityFeed\":1,\"scenceCode\":\"HEALTH_CHANNEL\",\"schemeParams\":{},\"scope\":\"PARTIAL\",\"selectedTabCode\":\"MD2024042300000013\",\"sourceType\":\"miniApp\",\"specialItemId\":\"\",\"specialItemType\":\"\",\"tenantCode\":\"2021003141652419\",\"underTakeContentId\":\"\"},\"version\":\"2.0\"}]"
     * );
     * }
     */

    public static String query_forest_energy() {
        return ApplicationHook.requestString("alipay.iblib.channel.data",
                "[{\"activityCode\":\"query_forest_energy\",\"activityId\":\"2024052300762675\",\"appId\":\"2021003141652419\",\"body\":{\"scene\":\"FEEDS\"},\"version\":\"2.0\"}]");
    }

    public static String produce_forest_energy(String uniqueId) {
        return ApplicationHook.requestString("alipay.iblib.channel.data",
                "[{\"activityCode\":\"produce_forest_energy\",\"activityId\":\"2024052300762674\",\"appId\":\"2021003141652419\",\"body\":{\"scene\":\"FEEDS\",\"uniqueId\":\""
                        + uniqueId + "\"},\"version\":\"2.0\"}]");
    }

    public static String harvest_forest_energy(int energy, String id) {
        return ApplicationHook.requestString("alipay.iblib.channel.data",
                "[{\"activityCode\":\"harvest_forest_energy\",\"activityId\":\"2024052300762676\",\"appId\":\"2021003141652419\",\"body\":{\"bubbles\":[{\"energy\":"
                        + energy + ",\"id\":\"" + id
                        + "\"}],\"scene\":\"FEEDS\"},\"version\":\"2.0\"}]");
    }

}