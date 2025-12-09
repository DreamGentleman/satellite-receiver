# BMCore

#### 介绍

BMCore库使用说明接口文档

#### 安装教程

1.  将aar包直接导入工程中lib文件夹下

2.  在app的Gradle文件中添加依赖
    dependencies {
        implementation files('libs/bmcore.aar')
    }
    
#### 使用教程
    
1.  先调用  BMEngine.init(Context context, String key, String customIconPath);  方法初始化地球配置
    
2.  然后再在Activity中创建EarthFragment用于显示地球
    
#### 接口说明

##### EarthFragment里面提供的可调用方法

###### 向轨迹上添加点

    /**
     * @param point    点
     * @param time     该点时间
     * @param speed    速度
     * @param accuracy 精度
     * @param isDevice 是否是设备
     */
    public void addPointToTrack(GeoPoint point, long time, double speed, double accuracy, boolean isDevice)

###### 跳转到指定位置
   
    /**
     * @param geoPoint 经纬度坐标
     * @param height   视点高度
     * @param time     跳转动画时间
     * @param pitch    视点倾斜角度
     */
    public void animateTo(GeoPoint geoPoint, int height, double time, double pitch)

###### 跳转到指定区域
   
    /**
     * @param box 范围区域
     */
    public void animateToArea(BoundingBox box)

###### 跳转到离线地图加载的区域
    
    public void animateToOfflineArea()

###### 线转面

    /**
     * @param id 线ID
     * @return 元素
     */
    public VectorElement changeLineToPlane(long id)

###### 改变地图源
    
    /**
     * @param source 地图源
     */
    public void changeMapSource(String source)

###### 改变地图源子类型
    
    /**
     * @param index 地图源子类型索引
     */
    public void changeMapSourceSubIndex(int index)

###### 改变地图类型
    
    /**
     * @param type 地图类型
     */
    public void changeMapType(int type)

###### 改变地图分组
   
    /**
     * @param group 地图分组
     */
    public void changeMapTypeGroup(int group)

###### 拖拽摇杆
    
    /**
     * @param type  摇杆拖拽方向
     * @param level 较原点位置位移的等级
     */
    public void dragRocker(int type, float level) 

###### 绘制元素
       
    /**
     * @param element     需要绘制的元素
     * @param isDeclutter 是否整理
     * @return elementId
     */
    public long drawElement(VectorElement element, boolean isDeclutter)

###### 绘制元素
       
    /**
     * @param elements 需要绘制的元素
     * @return 所有元素的id
     */
    public long[] drawElements(VectorElement[] elements)

###### 获取当前轨迹长度

    public int getCurrentTrackLength()

###### 获取元素的坐标

    /**
     * @param id 元素ID
     * @return 坐标
     */
    public List<GeoPoint> getElementGPS(long id)

###### 获取图层下的元素
    
    /**
     * @param layerId 所在图层的ID
     * @return 元素
     */
    public List<VectorElement> getElementsOfLayer(long layerId)

###### 获取格式化后的时间
    
    /**
     * @param time 整型时间
     * @return 格式化后的时间
     */
    public String getFormatHistoricalTime(int time)
    
###### 获取根图层的ID
   
    /**
     * @return 根图层的ID
     */
    public long getRootLayerId() 
    
###### 获取元素的属性

    /**
     * @param id 元素ID
     * @return element对象
     */
    public VectorElement getThisElementAttribute(long id)

###### 加载BMV文件

    /**
     * @param layerId 添加在改图层下
     * @param path    文件所在路径
     */
    public void loadBMVFile(long layerId, String path)
    
###### 加载KML文件

    /**
     * @param layerId 添加在改图层下
     * @param path    文件所在路径
     */
    public void loadKMLFile(long layerId, String path)
    
###### 将元素顺序改变或移动至其他图层
   
    /**
     * @param id      元素ID
     * @param layerId 图层ID
     * @param index   索引
     */
    public void moveElementTo(long id, long layerId, int index)
    
###### 取消正在编辑位置的元素，元素会回到编辑前的位置
    
    public void onCancelEditingElement() 

###### 创建图层
    
    /**
     * @param parentId 所在图层的ID
     * @param name     图层的名称
     * @return 所创建的图层ID
     */
    public VectorElement onCreateLayer(long parentId, String name, boolean visible)

###### 停止编辑元素
   
    public void onStopEditElement()
    
###### 停止漫游
    
    public void onStopRoaming()
    
###### 将元素从地球上移除

    /**
     * @param id 元素ID
     */
    public void removeElementFromEarth(long id)
    
###### 移除地图源

    /**
     * @param provider 地图源
     */
    public void removeOfflineProvider(Provider provider)
    
###### 重置摇杆
    
    public void resetRocker()
    
###### 记录轨迹完成

    /**
     * @param layerId 图层ID
     * @param isStop  是否是停止
     * @return 轨迹对象
     */
    public VectorElement recordTrackComplete(long layerId, boolean isStop)
    
###### 漫游摇杆
   
    /**
     * @param type 摇杆点击方向
     * @return 等级
     */
    public int roamingRocker(int type)

###### 设置方向罗盘是否可见
    
    /**
     * @param b 罗盘是否可见
     */
    public void setCompassVisible(boolean b)

###### 设置当前element所在图层
    
    /**
     * @param layerId 所在图层的ID
     */
    public void setDrawElementLayer(long layerId)
    
###### 设置元素的名称

    /**
     * @param id   元素ID
     * @param name 名称
     */
    public void setElementName(long id, String name)
    
###### 设置元素可见

    /**
     * @param id   元素ID
     * @param show 是否可见
     */
    public void setElementVisible(long id, boolean show)
    
###### 设置历史影像的时间
    
    /**
     * @param time 整型时间
     */
    public void setHistoricalTime(int time)

###### 开始记录轨迹

    public void startRecordingTrack()

###### 设置元素属性

    /**
     * @param element 元素
     */
    public void setThisElementAttribute(VectorElement element)

###### 取消绘制元素
   
    public void toCancelDrawingElement()
    
###### 清空绘制元素
    
    public void toDeleteDrawingElement()

###### 撤销正在绘制的元素上的点
    
    public void toRetreatDrawingElement()

###### 开始绘制元素
    
    /**
     * @param type     元素类型
     * @param parentId 所在图层ID
     */
    public void toStartDrawElement(int type, long parentId)

###### 停止正在绘制的元素
    
    /**
     * @return 是否绘制成功
     */
    public boolean toStopDrawElement()

###### 开始编辑元素位置
    
    /**
     * @param id 元素ID
     * @return 是否成功
     */
    public boolean toStartEditElement(long id)

###### 修改当前定位地点
    
    /**
     * @param geoPoint 经纬度坐标
     * @param accuracy 精度
     * @param heading  方向
     */
    public void updateLocation(GeoPoint geoPoint, double accuracy, double heading)

###### 放大地图
    
    public void zoomIn()

###### 缩小地图
    
    public void zoomOut()
    
##### BMEngine里面提供的可调用方法

###### 初始化

    /**
     * @param context 上下文
     * @param key appkey
     * @param customIconPath 自定义Icon路径
     */
    public static void init(Context context, String key, String customIconPath)
    
###### 获取地图源

    public static List<Provider> getMapProvider()
    
###### 获取行政边界根数据

    public static BorderData[] getBorderRootData()
    
###### 获取行政边界子数据
    
    public static BorderData[] getBorderChildData(String id, boolean needDraw)
    
###### 移除边界数据
    
    public static void removeBorderData()
    
###### 保存矢量数据
    
    public static void saveVectorFile(long id, String path, int type)
    
###### 保存矢量数据
    
    public static boolean saveMultiVectorFile(long[] id, String path, int type)

##### 回调接口

###### 创建地球完成

    onCreateEarthComplete()

###### 地球滑动

    onScroll()

###### 回调屏幕中心点坐标信息

    void callbackScreenCenterPoint(GeoPoint geoPoint, double height, long time)

###### 回调地球旋转方向

    void callbackEarthOrientation(float heading)

###### 点击地球

    void onSingleTapConfirmed(MotionEvent event)

###### 长按元素

    void onLongClickedElement(VectorElement element)

###### 切换地图源完成

    void onChangeMapSourceComplete(MapConfig config)

###### 切换地图源类型完成

    void onChangeMapTypeGroupComplete(MapConfig config)

###### 绘制元素Editing步骤

    void onCallbackDrawElementStepEditing(long id)

###### 绘制元素Created步骤

    void onCallbackDrawElementStepCreated(VectorElement element)

###### 地球滑动

    onScroll()

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request