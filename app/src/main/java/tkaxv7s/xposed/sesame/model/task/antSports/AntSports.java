package tkaxv7s.xposed.sesame.model.task.antSports;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import org.json.JSONArray;
import org.json.JSONObject;
import tkaxv7s.xposed.sesame.data.ModelFields;
import tkaxv7s.xposed.sesame.data.ModelGroup;
import tkaxv7s.xposed.sesame.data.modelFieldExt.BooleanModelField;
import tkaxv7s.xposed.sesame.data.modelFieldExt.ChoiceModelField;
import tkaxv7s.xposed.sesame.data.modelFieldExt.IntegerModelField;
import tkaxv7s.xposed.sesame.data.modelFieldExt.SelectModelField;
import tkaxv7s.xposed.sesame.data.task.ModelTask;
import tkaxv7s.xposed.sesame.entity.AlipayUser;
import tkaxv7s.xposed.sesame.hook.ApplicationHook;
import tkaxv7s.xposed.sesame.model.base.TaskCommon;
import tkaxv7s.xposed.sesame.model.normal.base.BaseModel;
import tkaxv7s.xposed.sesame.util.*;

import java.util.Calendar;
import java.util.LinkedHashSet;

public class AntSports extends ModelTask {

    private static final String TAG = AntSports.class.getSimpleName();

    private int tmpStepCount = -1;

    @Override
    public String getName() {
        return "运动";
    }

    @Override
    public ModelGroup getGroup() {
        return ModelGroup.SPORTS;
    }

    private BooleanModelField openTreasureBox;
    private BooleanModelField receiveCoinAsset;
    private BooleanModelField donateCharityCoin;
    private ChoiceModelField donateCharityCoinType;
    private IntegerModelField donateCharityCoinAmount;
    private IntegerModelField minExchangeCount;
    private IntegerModelField latestExchangeTime;
    private IntegerModelField syncStepCount;
    private BooleanModelField tiyubiz;
    private BooleanModelField battleForFriends;
    private ChoiceModelField battleForFriendType;
    private SelectModelField originBossIdList;

    @Override
    public ModelFields getFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(openTreasureBox = new BooleanModelField("openTreasureBox", "开启宝箱", false));
        modelFields.addField(receiveCoinAsset = new BooleanModelField("receiveCoinAsset", "收运动币", false));
        modelFields.addField(donateCharityCoin = new BooleanModelField("donateCharityCoin", "捐运动币 | 开启", false));
        modelFields.addField(donateCharityCoinType = new ChoiceModelField("donateCharityCoinType", "捐运动币 | 方式", DonateCharityCoinType.ONE, DonateCharityCoinType.nickNames));
        modelFields.addField(donateCharityCoinAmount = new IntegerModelField("donateCharityCoinAmount", "捐运动币 | 数量(每次)", 100));
        modelFields.addField(battleForFriends = new BooleanModelField("battleForFriends", "抢好友 | 开启", false));
        modelFields.addField(battleForFriendType = new ChoiceModelField("battleForFriendType", "抢好友 | 动作", BattleForFriendType.ROB, BattleForFriendType.nickNames));
        modelFields.addField(originBossIdList = new SelectModelField("originBossIdList", "抢好友 | 好友列表", new LinkedHashSet<>(), AlipayUser::getList));
        modelFields.addField(tiyubiz = new BooleanModelField("tiyubiz", "文体中心", false));
        modelFields.addField(minExchangeCount = new IntegerModelField("minExchangeCount", "最小捐步步数", 0));
        modelFields.addField(latestExchangeTime = new IntegerModelField("latestExchangeTime", "最晚捐步时间(24小时制)", 22));
        modelFields.addField(syncStepCount = new IntegerModelField("syncStepCount", "自定义同步步数", 22000));
        return modelFields;
    }

    @Override
    public void boot(ClassLoader classLoader) {
        try {
            XposedHelpers.findAndHookMethod("com.alibaba.health.pedometer.core.datasource.PedometerAgent", classLoader,
                    "readDailyStep", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            int originStep = (Integer) param.getResult();
                            int step = tmpStepCount();
                            if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 6 || originStep >= step) {
                                return;
                            }
                            param.setResult(step);
                        }
                    });
            Log.i(TAG, "hook readDailyStep successfully");
        } catch (Throwable t) {
            Log.i(TAG, "hook readDailyStep err:");
            Log.printStackTrace(TAG, t);
        }
    }

    @Override
    public Boolean check() {
        return !TaskCommon.IS_ENERGY_TIME;
    }

    @Override
    public void run() {
        try {
            if (Status.canSyncStepToday(UserIdMap.getCurrentUid()) && TimeUtil.isNowAfterOrCompareTimeStr("0600")) {
                addChildTask(new ChildModelTask("syncStep", () -> {
                    int step = tmpStepCount();
                    try {
                        ClassLoader classLoader = ApplicationHook.getClassLoader();
                        if ((Boolean) XposedHelpers.callMethod(XposedHelpers.callStaticMethod(classLoader.loadClass("com.alibaba.health.pedometer.intergation.rpc.RpcManager"), "a"), "a", new Object[]{step, Boolean.FALSE, "system"})) {
                            Log.other("同步步数🏃🏻‍♂️[" + step + "步]");
                        } else {
                            Log.record("同步运动步数失败:" + step);
                        }
                        Status.SyncStepToday(UserIdMap.getCurrentUid());
                    } catch (Throwable t) {
                        Log.printStackTrace(TAG, t);
                    }
                }));
            }
            ClassLoader loader = ApplicationHook.getClassLoader();
            if (openTreasureBox.getValue())
                queryMyHomePage(loader);

            if (receiveCoinAsset.getValue())
                receiveCoinAsset();

            if (donateCharityCoin.getValue() && Status.canDonateCharityCoin())
                queryProjectList(loader);

            if (minExchangeCount.getValue() > 0 && Status.canExchangeToday(UserIdMap.getCurrentUid()))
                queryWalkStep(loader);

            if (tiyubiz.getValue()) {
                userTaskGroupQuery("SPORTS_DAILY_SIGN_GROUP");
                userTaskGroupQuery("SPORTS_DAILY_GROUP");
                userTaskRightsReceive();
                pathFeatureQuery();
                participate();
            }

            if (battleForFriends.getValue()) {
                queryClubHome();
                queryTrainItem();
                buyMember();
            }
        } catch (Throwable t) {
            Log.i(TAG, "start.run err:");
            Log.printStackTrace(TAG, t);
        }
    }

    public int tmpStepCount() {
        if (tmpStepCount >= 0) {
            return tmpStepCount;
        }
        tmpStepCount = syncStepCount.getValue();
        if (tmpStepCount > 0) {
            tmpStepCount = RandomUtil.nextInt(tmpStepCount, tmpStepCount + 2000);
            if (tmpStepCount > 100000) {
                tmpStepCount = 100000;
            }
        }
        return tmpStepCount;
    }

    private void receiveCoinAsset() {
        try {
            String s = AntSportsRpcCall.queryCoinBubbleModule();
            JSONObject jo = new JSONObject(s);
            if (jo.getBoolean("success")) {
                JSONObject data = jo.getJSONObject("data");
                if (!data.has("receiveCoinBubbleList"))
                    return;
                JSONArray ja = data.getJSONArray("receiveCoinBubbleList");
                for (int i = 0; i < ja.length(); i++) {
                    jo = ja.getJSONObject(i);
                    String assetId = jo.getString("assetId");
                    int coinAmount = jo.getInt("coinAmount");
                    jo = new JSONObject(AntSportsRpcCall.receiveCoinAsset(assetId, coinAmount));
                    if (jo.getBoolean("success")) {
                        Log.other("收集金币💰[" + coinAmount + "个]");
                    } else {
                        Log.record("首页收集金币" + " " + jo);
                    }
                }
            } else {
                Log.i(TAG, s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "receiveCoinAsset err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void queryMyHomePage(ClassLoader loader) {
        try {
            String s = AntSportsRpcCall.queryMyHomePage();
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                s = jo.getString("pathJoinStatus");
                if ("GOING".equals(s)) {
                    if (jo.has("pathCompleteStatus")) {
                        if ("COMPLETED".equals(jo.getString("pathCompleteStatus"))) {
                            jo = new JSONObject(AntSportsRpcCall.queryBaseList());
                            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                                JSONArray allPathBaseInfoList = jo.getJSONArray("allPathBaseInfoList");
                                JSONArray otherAllPathBaseInfoList = jo.getJSONArray("otherAllPathBaseInfoList")
                                        .getJSONObject(0)
                                        .getJSONArray("allPathBaseInfoList");
                                join(loader, allPathBaseInfoList, otherAllPathBaseInfoList, "");
                            } else {
                                Log.i(TAG, jo.getString("resultDesc"));
                            }
                        }
                    } else {
                        String rankCacheKey = jo.getString("rankCacheKey");
                        JSONArray ja = jo.getJSONArray("treasureBoxModelList");
                        for (int i = 0; i < ja.length(); i++) {
                            parseTreasureBoxModel(loader, ja.getJSONObject(i), rankCacheKey);
                        }
                        JSONObject joPathRender = jo.getJSONObject("pathRenderModel");
                        String title = joPathRender.getString("title");
                        int minGoStepCount = joPathRender.getInt("minGoStepCount");
                        jo = jo.getJSONObject("dailyStepModel");
                        int consumeQuantity = jo.getInt("consumeQuantity");
                        int produceQuantity = jo.getInt("produceQuantity");
                        String day = jo.getString("day");
                        int canMoveStepCount = produceQuantity - consumeQuantity;
                        if (canMoveStepCount >= minGoStepCount) {
                            go(loader, day, rankCacheKey, canMoveStepCount, title);
                        }
                    }
                } else if ("NOT_JOIN".equals(s)) {
                    String firstJoinPathTitle = jo.getString("firstJoinPathTitle");
                    JSONArray allPathBaseInfoList = jo.getJSONArray("allPathBaseInfoList");
                    JSONArray otherAllPathBaseInfoList = jo.getJSONArray("otherAllPathBaseInfoList").getJSONObject(0)
                            .getJSONArray("allPathBaseInfoList");
                    join(loader, allPathBaseInfoList, otherAllPathBaseInfoList, firstJoinPathTitle);
                }
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryMyHomePage err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void join(ClassLoader loader, JSONArray allPathBaseInfoList, JSONArray otherAllPathBaseInfoList,
                      String firstJoinPathTitle) {
        try {
            int index = -1;
            String title = null;
            String pathId = null;
            JSONObject jo = new JSONObject();
            for (int i = allPathBaseInfoList.length() - 1; i >= 0; i--) {
                jo = allPathBaseInfoList.getJSONObject(i);
                if (jo.getBoolean("unlocked")) {
                    title = jo.getString("title");
                    pathId = jo.getString("pathId");
                    index = i;
                    break;
                }
            }
            if (index < 0 || index == allPathBaseInfoList.length() - 1) {
                for (int j = otherAllPathBaseInfoList.length() - 1; j >= 0; j--) {
                    jo = otherAllPathBaseInfoList.getJSONObject(j);
                    if (jo.getBoolean("unlocked")) {
                        if (j != otherAllPathBaseInfoList.length() - 1 || index != allPathBaseInfoList.length() - 1) {
                            title = jo.getString("title");
                            pathId = jo.getString("pathId");
                            index = j;
                        }
                        break;
                    }
                }
            }
            if (index >= 0) {
                String s;
                if (title.equals(firstJoinPathTitle)) {
                    s = AntSportsRpcCall.openAndJoinFirst();
                } else {
                    s = AntSportsRpcCall.join(pathId);
                }
                jo = new JSONObject(s);
                if ("SUCCESS".equals(jo.getString("resultCode"))) {
                    Log.other("加入线路🚶🏻‍♂️[" + title + "]");
                    queryMyHomePage(loader);
                } else {
                    Log.i(TAG, jo.getString("resultDesc"));
                }
            } else {
                Log.record("好像没有可走的线路了！");
            }
        } catch (Throwable t) {
            Log.i(TAG, "join err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void go(ClassLoader loader, String day, String rankCacheKey, int stepCount, String title) {
        try {
            String s = AntSportsRpcCall.go(day, rankCacheKey, stepCount);
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                Log.other("行走线路🚶🏻‍♂️[" + title + "]#前进了" + jo.getInt("goStepCount") + "步");
                boolean completed = "COMPLETED".equals(jo.getString("completeStatus"));
                JSONArray ja = jo.getJSONArray("allTreasureBoxModelList");
                for (int i = 0; i < ja.length(); i++) {
                    parseTreasureBoxModel(loader, ja.getJSONObject(i), rankCacheKey);
                }
                if (completed) {
                    Log.other("完成线路🚶🏻‍♂️[" + title + "]");
                    queryMyHomePage(loader);
                }
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "go err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void parseTreasureBoxModel(ClassLoader loader, JSONObject jo, String rankCacheKey) {
        try {
            String canOpenTime = jo.getString("canOpenTime");
            String issueTime = jo.getString("issueTime");
            String boxNo = jo.getString("boxNo");
            String userId = jo.getString("userId");
            if (canOpenTime.equals(issueTime)) {
                openTreasureBox(loader, boxNo, userId);
            } else {
                long cot = Long.parseLong(canOpenTime);
                long now = Long.parseLong(rankCacheKey);
                long delay = cot - now;
                if (delay <= 0) {
                    openTreasureBox(loader, boxNo, userId);
                    return;
                }
                if (delay < BaseModel.getCheckInterval().getValue()) {
                    String taskId = "BX|" + boxNo;
                    if (hasChildTask(taskId)) {
                        return;
                    }
                    Log.record("还有 " + delay + "ms 开运动宝箱");
                    addChildTask(new ChildModelTask(taskId, "BX", () -> {
                        Log.record("蹲点开箱开始");
                        long startTime = System.currentTimeMillis();
                        while (System.currentTimeMillis() - startTime < 5_000) {
                            if (openTreasureBox(loader, boxNo, userId) > 0) {
                                break;
                            }
                            TimeUtil.sleep(200);
                        }
                    }, System.currentTimeMillis() + delay));
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "parseTreasureBoxModel err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private int openTreasureBox(ClassLoader loader, String boxNo, String userId) {
        try {
            String s = AntSportsRpcCall.openTreasureBox(boxNo, userId);
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONArray ja = jo.getJSONArray("treasureBoxAwards");
                int num = 0;
                for (int i = 0; i < ja.length(); i++) {
                    jo = ja.getJSONObject(i);
                    num += jo.getInt("num");
                    Log.other("运动宝箱🎁[" + num + jo.getString("name") + "]");
                }
                return num;
            } else if ("TREASUREBOX_NOT_EXIST".equals(jo.getString("resultCode"))) {
                Log.record(jo.getString("resultDesc"));
                return 1;
            } else {
                Log.record(jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "openTreasureBox err:");
            Log.printStackTrace(TAG, t);
        }
        return 0;
    }

    private void queryProjectList(ClassLoader loader) {
        try {
            JSONObject jo = new JSONObject(AntSportsRpcCall.queryProjectList(0));
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                int charityCoinCount = jo.getInt("charityCoinCount");
                if (charityCoinCount < donateCharityCoinAmount.getValue()) {
                    return;
                }
                JSONArray ja = jo.getJSONObject("projectPage").getJSONArray("data");
                for (int i = 0; i < ja.length() && charityCoinCount >= donateCharityCoinAmount.getValue(); i++) {
                    jo = ja.getJSONObject(i).getJSONObject("basicModel");
                    if ("DONATE_COMPLETED".equals(jo.getString("footballFieldStatus"))) {
                        break;
                    }
                    donate(loader, donateCharityCoinAmount.getValue(), jo.getString("projectId"), jo.getString("title"));
                    Status.donateCharityCoin();
                    charityCoinCount -=  donateCharityCoinAmount.getValue();
                    if (donateCharityCoinType.getValue() == DonateCharityCoinType.ONE) {
                        break;
                    }
                }
            } else {
                Log.record(TAG);
                Log.i(jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryProjectList err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void donate(ClassLoader loader, int donateCharityCoin, String projectId, String title) {
        try {
            String s = AntSportsRpcCall.donate(donateCharityCoin, projectId);
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                Log.other("捐赠活动❤️[" + title + "][" + donateCharityCoin + "运动币]");
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "donate err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void queryWalkStep(ClassLoader loader) {
        try {
            String s = AntSportsRpcCall.queryWalkStep();
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                jo = jo.getJSONObject("dailyStepModel");
                int produceQuantity = jo.getInt("produceQuantity");
                int hour = Integer.parseInt(Log.getFormatTime().split(":")[0]);
                if (produceQuantity >= minExchangeCount.getValue() || hour >= latestExchangeTime.getValue()) {
                    s = AntSportsRpcCall.walkDonateSignInfo(produceQuantity);
                    s = AntSportsRpcCall.donateWalkHome(produceQuantity);
                    jo = new JSONObject(s);
                    if (!jo.getBoolean("isSuccess"))
                        return;
                    JSONObject walkDonateHomeModel = jo.getJSONObject("walkDonateHomeModel");
                    JSONObject walkUserInfoModel = walkDonateHomeModel.getJSONObject("walkUserInfoModel");
                    if (!walkUserInfoModel.has("exchangeFlag")) {
                        Status.exchangeToday(UserIdMap.getCurrentUid());
                        return;
                    }

                    String donateToken = walkDonateHomeModel.getString("donateToken");
                    JSONObject walkCharityActivityModel = walkDonateHomeModel.getJSONObject("walkCharityActivityModel");
                    String activityId = walkCharityActivityModel.getString("activityId");

                    s = AntSportsRpcCall.exchange(activityId, produceQuantity, donateToken);
                    jo = new JSONObject(s);
                    if (jo.getBoolean("isSuccess")) {
                        JSONObject donateExchangeResultModel = jo.getJSONObject("donateExchangeResultModel");
                        int userCount = donateExchangeResultModel.getInt("userCount");
                        double amount = donateExchangeResultModel.getJSONObject("userAmount").getDouble("amount");
                        Log.other("捐出活动❤️[" + userCount + "步]#兑换" + amount + "元公益金");
                        Status.exchangeToday(UserIdMap.getCurrentUid());

                    } else if (s.contains("已捐步")) {
                        Status.exchangeToday(UserIdMap.getCurrentUid());
                    } else {
                        Log.i(TAG, jo.getString("resultDesc"));
                    }
                }
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryWalkStep err:");
            Log.printStackTrace(TAG, t);
        }
    }

    /* 文体中心 */// SPORTS_DAILY_SIGN_GROUP SPORTS_DAILY_GROUP
    private void userTaskGroupQuery(String groupId) {
        try {
            String s = AntSportsRpcCall.userTaskGroupQuery(groupId);
            JSONObject jo = new JSONObject(s);
            if (jo.getBoolean("success")) {
                jo = jo.getJSONObject("group");
                JSONArray userTaskList = jo.getJSONArray("userTaskList");
                for (int i = 0; i < userTaskList.length(); i++) {
                    jo = userTaskList.getJSONObject(i);
                    if (!"TODO".equals(jo.getString("status")))
                        continue;
                    JSONObject taskInfo = jo.getJSONObject("taskInfo");
                    String bizType = taskInfo.getString("bizType");
                    String taskId = taskInfo.getString("taskId");
                    jo = new JSONObject(AntSportsRpcCall.userTaskComplete(bizType, taskId));
                    if (jo.getBoolean("success")) {
                        String taskName = taskInfo.optString("taskName", taskId);
                        Log.other("完成任务🧾[" + taskName + "]");
                    } else {
                        Log.record("文体每日任务" + " " + jo);
                    }
                }
            } else {
                Log.record("文体每日任务" + " " + s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "userTaskGroupQuery err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void participate() {
        try {
            String s = AntSportsRpcCall.queryAccount();
            JSONObject jo = new JSONObject(s);
            if (jo.getBoolean("success")) {
                double balance = jo.getDouble("balance");
                if (balance < 100)
                    return;
                jo = new JSONObject(AntSportsRpcCall.queryRoundList());
                if (jo.getBoolean("success")) {
                    JSONArray dataList = jo.getJSONArray("dataList");
                    for (int i = 0; i < dataList.length(); i++) {
                        jo = dataList.getJSONObject(i);
                        if (!"P".equals(jo.getString("status")))
                            continue;
                        if (jo.has("userRecord"))
                            continue;
                        JSONArray instanceList = jo.getJSONArray("instanceList");
                        int pointOptions = 0;
                        String roundId = jo.getString("id");
                        String InstanceId = null;
                        String ResultId = null;
                        for (int j = instanceList.length() - 1; j >= 0; j--) {
                            jo = instanceList.getJSONObject(j);
                            if (jo.getInt("pointOptions") < pointOptions)
                                continue;
                            pointOptions = jo.getInt("pointOptions");
                            InstanceId = jo.getString("id");
                            ResultId = jo.getString("instanceResultId");
                        }
                        jo = new JSONObject(AntSportsRpcCall.participate(pointOptions, InstanceId, ResultId, roundId));
                        if (jo.getBoolean("success")) {
                            jo = jo.getJSONObject("data");
                            String roundDescription = jo.getString("roundDescription");
                            int targetStepCount = jo.getInt("targetStepCount");
                            Log.other("走路挑战🚶🏻‍♂️[" + roundDescription + "]#" + targetStepCount);
                        } else {
                            Log.record("走路挑战赛" + " " + jo);
                        }
                    }
                } else {
                    Log.record("queryRoundList" + " " + jo);
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "participate err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void userTaskRightsReceive() {
        try {
            String s = AntSportsRpcCall.userTaskGroupQuery("SPORTS_DAILY_GROUP");
            JSONObject jo = new JSONObject(s);
            if (jo.getBoolean("success")) {
                jo = jo.getJSONObject("group");
                JSONArray userTaskList = jo.getJSONArray("userTaskList");
                for (int i = 0; i < userTaskList.length(); i++) {
                    jo = userTaskList.getJSONObject(i);
                    if (!"COMPLETED".equals(jo.getString("status")))
                        continue;
                    String userTaskId = jo.getString("userTaskId");
                    JSONObject taskInfo = jo.getJSONObject("taskInfo");
                    String taskId = taskInfo.getString("taskId");
                    jo = new JSONObject(AntSportsRpcCall.userTaskRightsReceive(taskId, userTaskId));
                    if (jo.getBoolean("success")) {
                        String taskName = taskInfo.optString("taskName", taskId);
                        JSONArray rightsRuleList = taskInfo.getJSONArray("rightsRuleList");
                        StringBuilder award = new StringBuilder();
                        for (int j = 0; j < rightsRuleList.length(); j++) {
                            jo = rightsRuleList.getJSONObject(j);
                            award.append(jo.getString("rightsName")).append("*").append(jo.getInt("baseAwardCount"));
                        }
                        Log.other("领取奖励🎖️[" + taskName + "]#" + award);
                    } else {
                        Log.record("文体中心领取奖励");
                        Log.i(jo.toString());
                    }
                }
            } else {
                Log.record("文体中心领取奖励");
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "userTaskRightsReceive err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void pathFeatureQuery() {
        try {
            String s = AntSportsRpcCall.pathFeatureQuery();
            JSONObject jo = new JSONObject(s);
            if (jo.getBoolean("success")) {
                JSONObject path = jo.getJSONObject("path");
                String pathId = path.getString("pathId");
                String title = path.getString("title");
                int minGoStepCount = path.getInt("minGoStepCount");
                if (jo.has("userPath")) {
                    JSONObject userPath = jo.getJSONObject("userPath");
                    String userPathRecordStatus = userPath.getString("userPathRecordStatus");
                    if ("COMPLETED".equals(userPathRecordStatus)) {
                        pathMapHomepage(pathId);
                        pathMapJoin(title, pathId);
                    } else if ("GOING".equals(userPathRecordStatus)) {
                        pathMapHomepage(pathId);
                        String countDate = Log.getFormatDate();
                        jo = new JSONObject(AntSportsRpcCall.stepQuery(countDate, pathId));
                        if (jo.getBoolean("success")) {
                            int canGoStepCount = jo.getInt("canGoStepCount");
                            if (canGoStepCount >= minGoStepCount) {
                                String userPathRecordId = userPath.getString("userPathRecordId");
                                tiyubizGo(countDate, title, canGoStepCount, pathId, userPathRecordId);
                            }
                        }
                    }
                } else {
                    pathMapJoin(title, pathId);
                }
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "pathFeatureQuery err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void pathMapHomepage(String pathId) {
        try {
            String s = AntSportsRpcCall.pathMapHomepage(pathId);
            JSONObject jo = new JSONObject(s);
            if (jo.getBoolean("success")) {
                if (!jo.has("userPathGoRewardList"))
                    return;
                JSONArray userPathGoRewardList = jo.getJSONArray("userPathGoRewardList");
                for (int i = 0; i < userPathGoRewardList.length(); i++) {
                    jo = userPathGoRewardList.getJSONObject(i);
                    if (!"UNRECEIVED".equals(jo.getString("status")))
                        continue;
                    String userPathRewardId = jo.getString("userPathRewardId");
                    jo = new JSONObject(AntSportsRpcCall.rewardReceive(pathId, userPathRewardId));
                    if (jo.getBoolean("success")) {
                        jo = jo.getJSONObject("userPathRewardDetail");
                        JSONArray rightsRuleList = jo.getJSONArray("userPathRewardRightsList");
                        StringBuilder award = new StringBuilder();
                        for (int j = 0; j < rightsRuleList.length(); j++) {
                            jo = rightsRuleList.getJSONObject(j).getJSONObject("rightsContent");
                            award.append(jo.getString("name")).append("*").append(jo.getInt("count"));
                        }
                        Log.other("文体宝箱🎁[" + award + "]");
                    } else {
                        Log.record("文体中心开宝箱");
                        Log.i(jo.toString());
                    }
                }
            } else {
                Log.record("文体中心开宝箱");
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "pathMapHomepage err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void pathMapJoin(String title, String pathId) {
        try {
            JSONObject jo = new JSONObject(AntSportsRpcCall.pathMapJoin(pathId));
            if (jo.getBoolean("success")) {
                Log.other("加入线路🚶🏻‍♂️[" + title + "]");
                pathFeatureQuery();
            } else {
                Log.i(TAG, jo.toString());
            }
        } catch (Throwable t) {
            Log.i(TAG, "pathMapJoin err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void tiyubizGo(String countDate, String title, int goStepCount, String pathId,
                           String userPathRecordId) {
        try {
            String s = AntSportsRpcCall.tiyubizGo(countDate, goStepCount, pathId, userPathRecordId);
            JSONObject jo = new JSONObject(s);
            if (jo.getBoolean("success")) {
                jo = jo.getJSONObject("userPath");
                Log.other("行走线路🚶🏻‍♂️[" + title + "]#前进了" + jo.getInt("userPathRecordForwardStepCount") + "步");
                pathMapHomepage(pathId);
                boolean completed = "COMPLETED".equals(jo.getString("userPathRecordStatus"));
                if (completed) {
                    Log.other("完成线路🚶🏻‍♂️[" + title + "]");
                    pathFeatureQuery();
                }
            } else {
                Log.i(TAG, s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "tiyubizGo err:");
            Log.printStackTrace(TAG, t);
        }
    }

    /* 抢好友大战 */
    private void queryClubHome() {
        try {
            // 发送 RPC 请求获取 club home 数据
            JSONObject clubHomeData = new JSONObject(AntSportsRpcCall.queryClubHome());
            // 处理 mainRoom 中的 bubbleList
            processBubbleList(clubHomeData.optJSONObject("mainRoom"));
            // 处理 roomList 中的每个房间的 bubbleList
            JSONArray roomList = clubHomeData.optJSONArray("roomList");
            if (roomList != null) {
                for (int i = 0; i < roomList.length(); i++) {
                    JSONObject room = roomList.optJSONObject(i);
                    processBubbleList(room);
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryClubHome err:");
            Log.printStackTrace(TAG, t);
        }
    }

    // 抢好友大战-收金币
    private void processBubbleList(JSONObject object) {
        if (object != null && object.has("bubbleList")) {
            try {
                JSONArray bubbleList = object.getJSONArray("bubbleList");
                for (int j = 0; j < bubbleList.length(); j++) {
                    JSONObject bubble = bubbleList.getJSONObject(j);
                    // 获取 bubbleId
                    String bubbleId = bubble.optString("bubbleId");
                    // 调用 collectBubble 方法
                    AntSportsRpcCall.collectBubble(bubbleId);
                    // 输出日志信息
                    int fullCoin = bubble.optInt("fullCoin");
                    Log.other("训练好友💰️[获得:" + fullCoin + "金币]");
                    // 添加 1 秒的等待时间
                    Thread.sleep(1000);
                }
            } catch (Throwable t) {
                Log.i(TAG, "processBubbleList err:");
                Log.printStackTrace(TAG, t);
            }
        }
    }

    // 抢好友大战-训练好友
    private void queryTrainItem() {
        try {
            // 发送 RPC 请求获取 club home 数据
            JSONObject clubHomeData = new JSONObject(AntSportsRpcCall.queryClubHome());
            // 检查是否存在 roomList
            if (clubHomeData.has("roomList")) {
                JSONArray roomList = clubHomeData.getJSONArray("roomList");
                // 遍历 roomList
                for (int i = 0; i < roomList.length(); i++) {
                    JSONObject room = roomList.getJSONObject(i);
                    // 获取 memberList
                    JSONArray memberList = room.getJSONArray("memberList");
                    // 遍历 memberList
                    for (int j = 0; j < memberList.length(); j++) {
                        JSONObject member = memberList.getJSONObject(j);
                        // 提取 memberId 和 originBossId
                        String memberId = member.getString("memberId");
                        String originBossId = member.getString("originBossId");
                        // 获取用户名称
                        String userName = UserIdMap.getMaskName(originBossId);
                        // 发送 RPC 请求获取 train item 数据
                        String responseData = AntSportsRpcCall.queryTrainItem();
                        // 解析 JSON 数据
                        JSONObject responseJson = new JSONObject(responseData);
                        // 检查请求是否成功
                        boolean success = responseJson.getBoolean("success");
                        if (!success) {
                            return;
                        }
                        // 获取 trainItemList
                        JSONArray trainItemList = responseJson.getJSONArray("trainItemList");
                        // 遍历 trainItemList
                        for (int k = 0; k < trainItemList.length(); k++) {
                            JSONObject trainItem = trainItemList.getJSONObject(k);
                            // 提取训练项目的相关信息
                            String itemType = trainItem.getString("itemType");
                            // 如果找到了 itemType 为 "barbell" 的训练项目，则调用 trainMember 方法并传递 itemType、memberId 和 originBossId 值
                            if ("barbell".equals(itemType)) {
                                // 调用 trainMember 方法并传递 itemType、memberId 和 originBossId 值
                                String trainMemberResponse = AntSportsRpcCall.trainMember(itemType, memberId, originBossId);
                                // 解析 trainMember 响应数据
                                JSONObject trainMemberResponseJson = new JSONObject(trainMemberResponse);
                                // 检查 trainMember 响应是否成功
                                boolean trainMemberSuccess = trainMemberResponseJson.getBoolean("success");
                                if (!trainMemberSuccess) {
                                    Log.i(TAG, "trainMember request failed");
                                    continue; // 如果 trainMember 请求失败，继续处理下一个训练项目
                                }
                                // 获取训练项目的名称
                                String trainItemName = trainItem.getString("name");
                                // 将用户名称和训练项目的名称添加到日志输出
                                Log.other("训练好友🥋[训练:" + userName + " " + trainItemName + "]");
                            }
                        }
                    }
                    // 添加 1 秒的间隔
                    Thread.sleep(1000);
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryTrainItem err:");
            Log.printStackTrace(TAG, t);
        }
    }

    // 抢好友大战-抢购好友
    private void buyMember() {
        try {
            // 发送 RPC 请求获取 club home 数据
            String clubHomeResponse = AntSportsRpcCall.queryClubHome();
            TimeUtil.sleep(500);
            JSONObject clubHomeJson = new JSONObject(clubHomeResponse);
            // 判断 clubAuth 字段是否为 "ENABLE"
            if (!clubHomeJson.optString("clubAuth").equals("ENABLE")) {
                // 如果 clubAuth 不是 "ENABLE"，停止执行
                Log.record("抢好友大战🧑‍🤝‍🧑未授权开启");
                return;
            }
            // 获取 coinBalance 的值
            JSONObject assetsInfo = clubHomeJson.getJSONObject("assetsInfo");
            int coinBalance = assetsInfo.getInt("coinBalance");
            JSONArray roomList = clubHomeJson.getJSONArray("roomList");
            // 遍历 roomList
            for (int i = 0; i < roomList.length(); i++) {
                JSONObject room = roomList.getJSONObject(i);
                JSONArray memberList = room.optJSONArray("memberList");
                // 检查 memberList 是否为空
                if (memberList == null || memberList.length() == 0) {
                    // 获取 roomId 的值
                    String roomId = room.getString("roomId");
                    // 调用 queryMemberPriceRanking 方法并传递 coinBalance 的值
                    String memberPriceResult = AntSportsRpcCall.queryMemberPriceRanking(String.valueOf(coinBalance));
                    TimeUtil.sleep(500);
                    JSONObject memberPriceJson = new JSONObject(memberPriceResult);
                    // 检查是否存在 rank 字段
                    if (memberPriceJson.has("rank") && memberPriceJson.getJSONObject("rank").has("data")) {
                        JSONArray dataArray = memberPriceJson.getJSONObject("rank").getJSONArray("data");
                        // 遍历 data 数组
                        for (int j = 0; j < dataArray.length(); j++) {
                            JSONObject dataObj = dataArray.getJSONObject(j);
                            String originBossId = dataObj.getString("originBossId");
                            // 检查 originBossId 是否在 originBossIdList 中
                            boolean isBattleForFriend = originBossIdList.getValue().contains(originBossId);
                            if (battleForFriendType.getValue() == BattleForFriendType.DONT_ROB) {
                                isBattleForFriend = !isBattleForFriend;
                            }
                            if (isBattleForFriend) {
                                // 在这里调用 queryClubMember 方法并传递 memberId 和 originBossId 的值
                                String clubMemberResult = AntSportsRpcCall.queryClubMember(dataObj.getString("memberId"), originBossId);
                                TimeUtil.sleep(500);
                                // 解析 queryClubMember 返回的 JSON 数据
                                JSONObject clubMemberJson = new JSONObject(clubMemberResult);
                                if (clubMemberJson.has("member")) {
                                    JSONObject memberObj = clubMemberJson.getJSONObject("member");
                                    // 获取当前成员的信息
                                    String currentBossId = memberObj.getString("currentBossId");
                                    String memberId = memberObj.getString("memberId");
                                    String priceInfo = memberObj.getString("priceInfo");
                                    // 调用 buyMember 方法
                                    String buyMemberResult = AntSportsRpcCall.buyMember(currentBossId, memberId, originBossId, priceInfo, roomId);
                                    TimeUtil.sleep(500);
                                    // 处理 buyMember 的返回结果
                                    JSONObject buyMemberResponse = new JSONObject(buyMemberResult);
                                    if ("SUCCESS".equals(buyMemberResponse.getString("resultCode"))) {
                                        String userName = UserIdMap.getMaskName(originBossId);
                                        Log.other("抢购好友🥋[成功:将 " + userName + " 抢回来]");
                                        // 执行训练好友
                                        queryTrainItem();
                                    } else if ("CLUB_AMOUNT_NOT_ENOUGH".equals(buyMemberResponse.getString("resultCode"))) {
                                        Log.record("[运动币不足，无法完成抢购好友！]");
                                    } else if ("CLUB_MEMBER_TRADE_PROTECT".equals(buyMemberResponse.getString("resultCode"))) {
                                        Log.record("[暂时无法抢购好友，给Ta一段独处的时间吧！]");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "buyMember err:");
            Log.printStackTrace(TAG, t);
        }
    }

    public interface DonateCharityCoinType {

        int ONE = 0;
        int ALL = 1;

        String[] nickNames = {"捐赠一个项目", "捐赠所有项目"};

    }

    public interface BattleForFriendType {

        int ROB = 0;
        int DONT_ROB = 1;

        String[] nickNames = {"选中抢", "选中不抢"};

    }
}