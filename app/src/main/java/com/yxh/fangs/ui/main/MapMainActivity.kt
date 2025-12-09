package com.yxh.fangs.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bigemap.bmcore.BMEngine
import com.bigemap.bmcore.EarthFragment
import com.bigemap.bmcore.constant.Constants
import com.bigemap.bmcore.entity.CustomMapSource
import com.bigemap.bmcore.entity.DefaultStyle
import com.bigemap.bmcore.entity.GeoPoint
import com.bigemap.bmcore.entity.MapConfig
import com.bigemap.bmcore.entity.VectorElement
import com.bigemap.bmcore.listener.OperationCallback
import com.bigemap.bmcore.sp.StyleUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.yxh.fangs.R
import com.yxh.fangs.util.Utils.copyAssets
import java.io.File

class MapMainActivity : BaseActivity(), OperationCallback {
  private var mEarthFragment: EarthFragment? = null
  // 添加一个变量标记地图是否已加载
  private var isEarthReady = false
  private var longitudeData: Double = 0.0
  private var latitudeData: Double = 0.0


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main_map)

    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    // 初始化位置请求
    locationRequest = LocationRequest.Builder(
      Priority.PRIORITY_HIGH_ACCURACY,
      5000L // 每 5 秒更新一次
    ).build()
    // 定位回调
    locationCallback = object : LocationCallback() {
      override fun onLocationResult(result: LocationResult) {
        for (location in result.locations) {
          longitudeData = location.longitude // 经度
          latitudeData = location.latitude // 纬度
          val text = "当前经度：${location.longitude}\n当前纬度：${location.latitude}"
          Toast.makeText(this@MapMainActivity, text, Toast.LENGTH_SHORT).show()
          if (isEarthReady) {
            onAnimateTo(longitudeData, latitudeData, 0.0)
            isEarthReady = false // 避免每次都跳转
          }
        }
      }
    }
    // 检查并请求权限
    checkPermissionAndStartLocation()

    // 初始化地图配置
    BMEngine.preInit(this, "bda2ea3fb18fdd9a4a6d922389576df7")
    // 1、Context 2、自定义图标存放位置 3、是否加载地形
    BMEngine.init(this, filesDir.path + File.separator, false)

    //拷贝文件到文件系统
    copyAssets(this, "img", filesDir.path)
    copyAssets(this, "map", filesDir.path)

    // Utils.INSTANCE.copyAssets(this, "同名图标", BMEngine.getIconPath());

    // 添加在线地图
    //addMapSource(TEST_MAP_SOURCE_URL3)
    //addMapSourceList()
    // 添加离线地图
    //addOfflineMap()

    mEarthFragment = EarthFragment.getInstance(this)

    val transaction = supportFragmentManager.beginTransaction()
    transaction.add(R.id.flt_container, mEarthFragment!!, TAG_EARTH_FRAGMENT)
    transaction.commitAllowingStateLoss()

    // 点击事件
    initCLicked()
  }

  private fun initCLicked() {
    findViewById<View>(R.id.offlineTv).setOnClickListener { changeOfflineMapSource() } // 离线地图加载
    findViewById<View>(R.id.onlineTv).setOnClickListener { changeOnlineMapSource(1) } // 在线地图加载
    findViewById<View>(R.id.locationTv).setOnClickListener { updateLocation() } // 当前位置
    findViewById<View>(R.id.lineTv).setOnClickListener { toAddLineInMap() } // 画线
    findViewById<View>(R.id.drawLineTv).setOnClickListener { onDrawLineElement() } // 手画线
    findViewById<View>(R.id.pointTv).setOnClickListener { toAddPointInMap() } // 画点
    findViewById<View>(R.id.drawPointTv).setOnClickListener { onDrawPointElement() } // 手画点
    findViewById<View>(R.id.drawPlaneTv).setOnClickListener { onDrawPlaneElement() } // 手画面
    findViewById<View>(R.id.drawRevocationTv).setOnClickListener { toRetreatDrawingElement() } // 撤销当前线绘制
    findViewById<View>(R.id.typhoonTv).setOnClickListener { startTyphoonSimulation() } // 模拟台风轨迹风圈绘制
  }

  // 动态权限加载
  private fun checkPermissionAndStartLocation() {
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
      != PackageManager.PERMISSION_GRANTED
    ) {
      ActivityCompat.requestPermissions(
        this,
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
        100
      )
    } else {
      startLocationUpdates()
    }
  }

  // 已授权，授权之后在加载地图
  private fun startLocationUpdates() {
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
      != PackageManager.PERMISSION_GRANTED
    ) return
    fusedLocationClient.requestLocationUpdates(
      locationRequest,
      locationCallback,
      Looper.getMainLooper()
    )
    // 添加在线地图
    addMapSource(TEST_MAP_SOURCE_URL3)
    addMapSourceList()
    // 添加离线地图
    addOfflineMap()
  }

  // 权限回调
  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == 100 && grantResults.isNotEmpty() &&
      grantResults[0] == PackageManager.PERMISSION_GRANTED
    ) {
      startLocationUpdates()
    } else {
      Toast.makeText(this, "未授予定位权限，无法获取位置", Toast.LENGTH_SHORT).show()
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    fusedLocationClient.removeLocationUpdates(locationCallback)
  }

  fun addMapSource(url: String?) {
    // 1、创建自定义地图源
    val custom = CustomMapSource()
    custom.name = "Arcgis"
    custom.url = url // 瓦片链接地址
    custom.tileSize = 256 // 瓦片大小
    custom.projection = true // 投影true:墨卡托，false:经纬度直投
    custom.minLevel = 0 // 最小层级
    custom.maxLevel = 19 // 最大层级
    custom.cacheKey = "ArcGis" // 缓存标识
    // 2、加入引擎中
    BMEngine.addCustomMapSource(custom)
  }

  private fun addMapSourceList() {
    // 1、创建自定义图层1
    val custom1 = CustomMapSource()
    custom1.name = "高德影像"
    custom1.url = TEST_MAP_SOURCE_URL1 // 瓦片链接地址
    custom1.tileSize = 256 // 瓦片大小
    custom1.projection = true // 投影true:墨卡托，false:经纬度直投
    custom1.minLevel = 0 // 最小层级
    custom1.maxLevel = 19 // 最大层级
    custom1.cacheKey = "高德影像" // 缓存标识
    // 2、创建自定义图层2
    val custom2 = CustomMapSource()
    custom2.name = "高德街道"
    custom2.url = TEST_MAP_SOURCE_URL2 // 瓦片链接地址
    custom2.tileSize = 256 // 瓦片大小
    custom2.projection = true // 投影true:墨卡托，false:经纬度直投
    custom2.minLevel = 0 // 最小层级
    custom2.maxLevel = 19 // 最大层级
    custom2.cacheKey = "高德街道" // 缓存标识
    val list: MutableList<CustomMapSource?> = ArrayList<CustomMapSource?>()
    list.add(custom1)
    list.add(custom2)
    // 2、加入引擎中
    BMEngine.addMapLayerList("地图源", "可以空", list)
  }

  fun addOfflineMap() {
    val name = "澳门特别行政区_卫图"
    val icon = ""
    val path = getFilesDir().getPath() + File.separator + "澳门特别行政区_卫图.bmpkg"
    val strings: MutableList<String?> = ArrayList<String?>()
    strings.add(path)
    BMEngine.addOfflineMap(name, icon, strings)
  }



  // 工具：把十六进制颜色（#RRGGBB 或 #AARRGGBB）加入 alpha
  private fun colorWithAlpha(hexColor: String, alphaInt: Int): String {
    // alphaInt: 0..255
    val alpha = "%02X".format(alphaInt.coerceIn(0,255))
    val c = hexColor.removePrefix("#")
    return if (c.length == 6) "#$alpha$c" else if (c.length == 8) "#$alpha${c.substring(2)}" else "#$alpha$c"
  }

  /**
   * 绘制风圈（用若干同心圆模拟填充）
   * lon,lat: 圆心经纬度
   * color: 形如 "#RRGGBB" 或 "#AARRGGBB"
   * radiusMeters: 半径（米）
   */
  fun addTyphoonCircleSimulatedFill(lon: Double, lat: Double, color: String, radiusMeters: Double) {
    val rootID = mEarthFragment!!.rootLayerId
    // 创建独立图层，用于这一组同心圆（方便后续管理/删除）
    val layer = mEarthFragment!!.onCreateLayer(rootID, "台风风圈_${System.currentTimeMillis()}", true)
    // 我们绘制若干圈来模拟填充（外圈：较粗，内圈逐步减小并加大透明度）
    val rings = 6
    for (i in 0 until rings) {
      // 从外到内：外圈透明度小一点，内圈更不透明（或反向根据需求调整）
      val t = i.toDouble() / (rings - 1) // 0..1
      // 计算每圈半径，从大到小
      val r = radiusMeters * (1.0 - 0.8 * t) // 最内圈为 radius * 0.2
      // 透明度：外圈更透明(例如 40)，内圈更不透明(例如 200)
      val alpha = (40 + (160 * (1.0 - t))).toInt() // 40 .. 200 (可调)
      val colorWithA = colorWithAlpha(color, alpha)
      val vector = VectorElement(layer.id, VectorElement.TYPE_CIRCLE, "风圈_ring_$i")
      // 使用每个元素的 outlineColor (每个 element 都可单独设)
      vector.outlineColor = colorWithA
      vector.outlineWidth = (if (i == 0) "6" else "2") // 外圈粗一些
      vector.showLabel = false
      // 一些 SDK 里有 description 可作为 label 内容（但 label 显示取决于引擎）
      vector.description = "风圈"
      // 圆心 + 半径
      val geo1 = GeoPoint(lon, lat, 0.0)
      val geo2 = GeoPoint(r, 0.0, 0.0)
      vector.geoPoints.add(geo1)
      vector.geoPoints.add(geo2)
      mEarthFragment!!.drawElement(vector, true)
    }
  }

  // 绘制台风路径
  fun drawTyphoonTrack(trackPoints: List<GeoPoint>) {
    val rootID = mEarthFragment!!.rootLayerId
    val layer = mEarthFragment!!.onCreateLayer(rootID, "台风轨迹", true)
    val vector = VectorElement(layer.id, VectorElement.TYPE_LINE, "台风路径")
    vector.outlineWidth = "5"
    vector.outlineColor = "#FF00BFFF" // 深天蓝
    vector.geoPoints.addAll(trackPoints)
    mEarthFragment!!.drawElement(vector, true)
  }

  // 绘制固定大小的风圈（不叠加、不渐变）
  fun addTyphoonCircle(lon: Double, lat: Double, color: String, radiusMeters: Double) {
    val rootID = mEarthFragment!!.rootLayerId
    val layer = mEarthFragment!!.onCreateLayer(rootID, "台风风圈_${System.currentTimeMillis()}", true)
    val vector = VectorElement(layer.id, VectorElement.TYPE_CIRCLE, "风圈")
    vector.outlineColor = color     // 每个风圈独立颜色
    vector.outlineWidth = "2"       // 线宽
    vector.showLabel = true        // false不显示标签
    vector.description = "风圈"
    // 圆心 + 半径（注意半径固定）
    val geo1 = GeoPoint(lon, lat, 0.0)
    val geo2 = GeoPoint(radiusMeters, 0.0, 0.0)
    vector.geoPoints.add(geo1)
    vector.geoPoints.add(geo2)
    mEarthFragment!!.drawElement(vector, true)
  }

  // 模拟台风移动 + 动态更新等级与风圈
  fun startTyphoonSimulation() {
    val handler = Handler(Looper.getMainLooper())
    val trackPoints = mutableListOf<GeoPoint>()
    // 模拟起点
    val baseLon = 123.0
    val baseLat = 18.0
    // 模拟台风强度变化阶段
    val levels = listOf("热带风暴", "强热带风暴", "台风", "强台风", "超强台风")
    for (i in levels.indices) {
      handler.postDelayed({
        val lon = longitudeData + i * 0.5
        val lat = latitudeData + i * 0.3
        trackPoints.add(GeoPoint(lon, lat))
        // 更新轨迹线
        drawTyphoonTrack(trackPoints)
        // 等级定义
        val level = levels[i]
        val (color, radius) = when (level) {
          "热带风暴" -> Pair("#FFFF00", 30000.0)
          "强热带风暴" -> Pair("#FFA500", 60000.0)
          "台风" -> Pair("#FF0000", 90000.0)
          "强台风" -> Pair("#8B0000", 120000.0)
          "超强台风" -> Pair("#800080", 150000.0)
          else -> Pair("#00FF00", 20000.0)
        }
        // 绘制风圈
        addTyphoonCircle(lon, lat, color, radius)
        // 移动视角到当前点
        //onAnimateTo(lon, lat, 1.0)
        // 根据台风登记预警
        if (level == "强台风") {
          Toast.makeText(this@MapMainActivity, "警告警告", Toast.LENGTH_SHORT).show()
        }
        Log.e("Fangs", "台风等级：$level\n经度:$lon, 纬度:$lat")
      }, (i * 2000L))
    }
  }











  private fun toAddPointInMap(lon: Double, lat: Double) {
    // 1、首先应该获取根图层ID
    val rootID = mEarthFragment!!.getRootLayerId()
    // 2、在根图层上创建自己的图层
    val layer = mEarthFragment!!.onCreateLayer(rootID, "", true)
    // 3、在自己创建的图层上添加元素
    val vector = VectorElement(layer.id, VectorElement.TYPE_POINT, "点")
    vector.description = "描述"
    // 4、是否使用自定义图标
    vector.isCustomPath = false
    if (vector.isCustomPath) {
      // 是：需要在自定义图标目录里面添加图标
      // BMEngine.init(this, 自定义图标目录, false)
      vector.iconPath = "自定义.png" // 只需要文件名
    } else {
      // 否：内置图标
      vector.iconPath = "170.png" // 只需要文件名
    }
    // 5、设置图标的大小
    vector.iconScale = 1.5f
    // 6、设置图标的对其方式
    vector.iconAlign = Constants.ICON_ALIGNMENT_CENTER_CENTER
    // 7、设置元素文本
    vector.showLabel = true
    vector.labelColor = "#FF00FF00"
    // 8、设置坐标
    val geo = GeoPoint(lon, lat, 0.0)
    vector.geoPoints.add(geo)
    // 9、绘制在地球中
    val id = mEarthFragment!!.drawElement(vector, true)
    BMEngine.setElementDescription(id, "测试")
  }

  private fun toAddLineInMap(geoPoint1: GeoPoint?, geoPoint2: GeoPoint?) {
    // 1、首先应该获取根图层ID
    val rootID = mEarthFragment!!.getRootLayerId()
    // 2、在根图层上创建自己的图层
    val layer = mEarthFragment!!.onCreateLayer(rootID, "", true)
    // 3、在自己创建的图层上添加元素
    val vector = VectorElement(layer.id, VectorElement.TYPE_LINE, "线")
    vector.description = "描述"
    // 4、设置线样式
    vector.outlineWidth = "5"
    vector.outlineColor = "#FF00FF00"
    // 5、设置元素文本
    vector.showLabel = true
    vector.labelColor = "#FF00FF00"
    // 6、设置坐标
    vector.geoPoints.add(geoPoint1)
    vector.geoPoints.add(geoPoint2)
    // 7、绘制在地球中
    val id = mEarthFragment!!.drawElement(vector, true)
    // 8、继续绘制
    Handler().postDelayed({
      val element = mEarthFragment!!.getThisElementAttribute(id)
      element.geoPoints.add(GeoPoint(113.565, 22.161))
      element.outlineColor = "#FFFF0000"
      mEarthFragment!!.setThisElementAttribute(element)

      Handler().postDelayed({
        val element = mEarthFragment!!.getThisElementAttribute(id)
        element.geoPoints.add(GeoPoint(113.566, 22.162))
        element.outlineColor = "#FF00FF00"
        mEarthFragment!!.setThisElementAttribute(element)

        Handler().postDelayed({
          val element = mEarthFragment!!.getThisElementAttribute(id)
          element.geoPoints.add(GeoPoint(113.567, 22.161))
          element.outlineColor = "#FFFF0000"
          mEarthFragment!!.setThisElementAttribute(element)
        }, 2000)
      }, 2000)
    }, 2000)
  }

  private fun toAddCircleInMap(lon: Double, lat: Double) {
    // 1、首先应该获取根图层ID
    val rootID = mEarthFragment!!.rootLayerId
    // 2、在根图层上创建自己的图层
    val layer = mEarthFragment!!.onCreateLayer(rootID, "", true)
    // 3、在自己创建的图层上添加元素
    val vector = VectorElement(layer.id, VectorElement.TYPE_CIRCLE, "圆")
    vector.description = "描述"
    // 4、设置元素文本
    vector.showLabel = true
    vector.labelColor = "#FF00FF00"
    // 5、设置坐标
    val geo1 = GeoPoint(lon, lat, 0.0)
    val geo2 = GeoPoint(1000.0, 0.0, 0.0)
    vector.geoPoints.add(geo1)
    vector.geoPoints.add(geo2)
    // 6、绘制在地球中
    mEarthFragment!!.drawElement(vector, true)
  }

  private fun toAddEllipseInMap(lon: Double, lat: Double) {
    // 1、首先应该获取根图层ID
    val rootID = mEarthFragment!!.getRootLayerId()
    // 2、在根图层上创建自己的图层
    val layer = mEarthFragment!!.onCreateLayer(rootID, "", true)
    // 3、在自己创建的图层上添加元素
    val vector = VectorElement(layer.id, VectorElement.TYPE_ELLIPSE, "椭圆")
    vector.description = "描述"
    // 4、设置元素文本
    vector.showLabel = true
    vector.labelColor = "#FF00FF00"
    // 5、设置坐标
    val geo1 = GeoPoint(lon, lat, 0.0)
    val geo2 = GeoPoint(1000.0, 500.0, 0.0) // lon长半轴 lat短半轴
    val geo3 = GeoPoint(12.0, 0.0, 0.0) // lon角度
    vector.geoPoints.add(geo1)
    vector.geoPoints.add(geo2)
    vector.geoPoints.add(geo3)
    // 6、绘制在地球中
    mEarthFragment!!.drawElement(vector, true)
  }

  private fun onDrawElement(type: Int) {
    StyleUtils.init(this)
    val style = DefaultStyle()
    style.polylineColor = BMEngine.argb2rgba("#FFFF0000")
    style.polylineWidth = 2f
    BMEngine.setDefaultStyle(style)
    // 1、首先应该获取根图层ID
    val rootID = mEarthFragment!!.rootLayerId
    // 2、在根图层上创建自己的图层
    val layer = mEarthFragment!!.onCreateLayer(rootID, "", true)
    // 3、在自己创建的图层上去绘制元素
    mEarthFragment!!.toStartDrawElement(type, layer.id)
    // 4、在地图上绘制完成后，会回调 onCallbackDrawElementStepEditing() 这个方法
  }


  override fun onCallbackDrawElementStepEditing(vectorElement: VectorElement?) {
    runOnUiThread { // 5、结束当前绘制对象，启动新的绘制
      mEarthFragment!!.toStopDrawElement()
      // 6、退出绘制
      mEarthFragment!!.toCancelDrawingElement()
      Log.e("Fangs", "==onCallbackDrawElementStepEditing==")
    }
  }

  override fun onCallbackDrawElementStepCreated(vectorElement: VectorElement?) {
    Log.e("Fangs", "==onCallbackDrawElementStepCreated==")
  }


  // time 跳转时间
  private fun onAnimateTo(lon: Double, lat: Double, time: Double) {
    // 定位位置
    val geoPoint = GeoPoint(lon, lat)
    // 视角高度
    val height = 3000
    // 俯仰角度
    val pitch = -90.0
    mEarthFragment!!.animateTo(geoPoint, height, time, pitch)
  }

  // 在线地图
  override fun onCreateEarthComplete() {
    Log.e("Fangs", "=====")
    isEarthReady = true
    // 获取内置地图源
    val providers = BMEngine.getMapProviders()
    if (!providers.isEmpty()) {
      val provider = providers.get(1) // 1、内置在线地图
      mEarthFragment!!.changeMapSource(provider.mapId)
      if (provider.mapId.startsWith("MAPID_BM_OFFLINEMAP_BKG")) {
        mEarthFragment!!.animateToOfflineArea()
      }
    }
    // BMEngine.setGesturesRotation(false); // 禁止旋转手势
    // BMEngine.setLockAzimuthPanning(true); // 锁定轴不转动
    // BMEngine.setGesturesTilting(false) // 让地球一直俯视

    // 动态获取经纬度 跳转指定位置
    if (longitudeData == 0.0 && latitudeData == 0.0) {
      onAnimateTo(113.5, 22.2, 0.0) // 默认移动到某个安全区域
    } else {
      onAnimateTo(longitudeData, latitudeData, 0.0)
    }

    BMEngine.isShowBuilding(false)
    startTyphoonSimulation()
  }

  override fun onCreateEarthFail(i: Int) {
  }

  override fun onScroll() {
  }

  override fun callbackEarthOrientation(v: Float) {
  }

  override fun callbackScreenCenterPoint(geoPoint: GeoPoint?, v: Double, l: Long, i: Int) {
    //        if (height < 1150) {
//            mZoom = 18;
//        } else if (height < 2300) {
//            mZoom = 17;
//        } else if (height < 4600) {
//            mZoom = 16;
//        } else if (height < 9200) {
//            mZoom = 15;
//        } else if (height < 18000) {
//            mZoom = 14;
//        } else if (height < 37000) {
//            mZoom = 13;
//        } else if (height < 75000) {
//            mZoom = 12;
//        } else if (height < 150000) {
//            mZoom = 11;
//        } else if (height < 300000) {
//            mZoom = 10;
//        } else if (height < 600000) {
//            mZoom = 9;
//        } else if (height < 1200000) {
//            mZoom = 8;
//        } else if (height < 2400000) {
//            mZoom = 7;
//        } else if (height < 4800000) {
//            mZoom = 6;
//        } else if (height < 9600000) {
//            mZoom = 5;
//        }
  }

  override fun onSingleTapConfirmed(motionEvent: MotionEvent?, geoPoint: GeoPoint?) {
  }

  override fun onLongPress(motionEvent: MotionEvent?, geoPoint: GeoPoint?) {
  }

  override fun onCallbackSiWeiHistoryData(strings: Array<String?>?) {
  }

  override fun onClickedElement(vectorElement: VectorElement) {
    val element = mEarthFragment!!.getThisElementAttribute(vectorElement.id)
  }

  override fun onLongClickedElement(vectorElement: VectorElement) {
    val element = mEarthFragment!!.getThisElementAttribute(vectorElement.id)
  }

  override fun onChangeMapSourceComplete(mapConfig: MapConfig?) {
  }

  override fun onChangeMapTypeGroupComplete(mapConfig: MapConfig?) {
  }

  override fun onCallbackHistoricalImagery(ints: IntArray?) {
  }

  override fun onCallbackHistoricalImagery(strings: Array<String?>?) {
  }

  override fun onCallbackAddedTrackPoint(geoPoint: GeoPoint?) {
  }

  override fun onLoadVectorFileStart(i: Int) {
  }

  override fun onLoadVectorFileDoing() {
  }

  override fun onLoadVectorFileComplete(b: Boolean, l: Long) {
  }

  override fun onLoadVectorFileComplete(vectorElement: VectorElement?) {
  }

  override fun onFormatStringToPicture(s: String?): ByteArray? {
    return ByteArray(0)
  }

  override fun webPToPng(bytes: ByteArray?): ByteArray? {
    return ByteArray(0)
  }

  override fun onUpdateOfflineCallback(i: Int, i1: Int): Boolean {
    return false
  }

  // 设置在线地图加载
  fun changeOnlineMapSource(index: Int) {
    val providers = BMEngine.getMapProviders()
    if (!providers.isEmpty()) {
      val provider = providers.get(index)
      mEarthFragment!!.changeMapSource(provider.mapId)
    }
  }

  // 离线地图加载
  fun changeOfflineMapSource() {
    val providers = BMEngine.getMapProviders()

    if (!providers.isEmpty()) {
      val provider = providers.get(providers.size - 1)
      mEarthFragment!!.changeMapSource(provider.mapId)

      if (provider.mapId.startsWith("MAPID_BM_OFFLINEMAP_PKG")) {
        mEarthFragment!!.animateToOfflineArea()
      }
      // 动态获取经纬度
      toAddPointInMap(longitudeData, latitudeData)
      onAnimateTo(longitudeData, latitudeData, 0.0)

    }
  }

  fun toTestAnimateTo() {
    // 动态获取经纬度
    toAddPointInMap(longitudeData, latitudeData)
    onAnimateTo(longitudeData, latitudeData, 3.0)
  }

  fun updateLocation() {
    val geoPoint = GeoPoint(longitudeData, latitudeData)// 动态获取经纬度
    Toast.makeText(
      this@MapMainActivity,
      "当前经度：$longitudeData\n当前纬度：$latitudeData",
      Toast.LENGTH_SHORT
    ).show()
    mEarthFragment!!.updateLocation(geoPoint, 100.0, 0.0) // 1、位置 2、精度 3、方向
  }

  // 画点
  fun toAddPointInMap() {
    // 动态获取经纬度
    toAddPointInMap(longitudeData, latitudeData)
    onAnimateTo(longitudeData, latitudeData, 3.0)
  }

  // 画线
  fun toAddLineInMap() {
    // 动态获取经纬度
    val geoPoint1 = GeoPoint(longitudeData, latitudeData)
    val geoPoint2 = GeoPoint(longitudeData + 0.000001, latitudeData + 0.000001)
    toAddLineInMap(geoPoint1, geoPoint2)
    onAnimateTo(longitudeData, latitudeData, 0.0)
  }

  // 添加圆
  fun toAddCircleInMap() {
    // 动态获取经纬度
    toAddCircleInMap(longitudeData, latitudeData)
    onAnimateTo(longitudeData, latitudeData, 0.0)
  }

  // 添加椭圆
  fun toAddEllipseInMap() {
    // 动态获取经纬度
    toAddEllipseInMap(longitudeData, latitudeData)
    onAnimateTo(longitudeData, latitudeData, 0.0)
  }

  // 手画点
  fun onDrawPointElement() {
    onDrawElement(Constants.DRAW_ELEMENT_TYPE_POINT)
    Toast.makeText(application, "点击屏幕", Toast.LENGTH_LONG).show()
  }

  // 手画线
  fun onDrawLineElement() {
    onDrawElement(Constants.DRAW_ELEMENT_TYPE_LINE)
    Toast.makeText(application, "点击屏幕", Toast.LENGTH_LONG).show()
  }

  // 手画面
  fun onDrawPlaneElement() {
    onDrawElement(Constants.DRAW_ELEMENT_TYPE_PLANE)
    Toast.makeText(application, "点击屏幕", Toast.LENGTH_LONG).show()
  }

  // 撤销当前线绘制
  fun toRetreatDrawingElement() {
    mEarthFragment!!.toRetreatDrawingElement()
  }

  fun loadKMLFile() {
    val url = ""
    if (url.isEmpty()) {
      Toast.makeText(application, "添加路径", Toast.LENGTH_LONG).show()
    } else {
      val rootID = mEarthFragment!!.rootLayerId
      mEarthFragment!!.loadKMLFile(rootID, url)
    }
  }

  companion object {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private const val TAG_EARTH_FRAGMENT = "TAG_EARTH_FRAGMENT"
    private const val TAG_PAGE_ONE_FRAGMENT = "TAG_PAGE_ONE_FRAGMENT"
    private const val TAG_PAGE_TWO_FRAGMENT = "TAG_PAGE_TWO_FRAGMENT"

    private const val TEST_MAP_SOURCE_URL =
      "http://services.arcgisonline.com/ArcGIS/services/World_Imagery/MapServer?mapname=Layers&layer=_alllayers&format=PNG&level={z}&row={y}&column={x}"
    private const val TEST_MAP_SOURCE_URL1 =
      "https://webst0[1-4].is.autonavi.com/appmaptile?x={x}&y={y}&z={z}&style=6"
    private const val TEST_MAP_SOURCE_URL2 =
      "https://wprd0[1-4].is.autonavi.com/appmaptile?x={x}&y={y}&z={z}&style=8"
    private const val TEST_MAP_SOURCE_URL3 =
      "https://hssk.hngqyun.cn:9000/bigemap.6h6bjjiu/tiles/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoiY3VzXzZucHJ1OXNmIiwiYSI6IjA3N2Fnc3F3OTN3dW03OXhtN2VtNDB0dnAiLCJ0Ijo0fQ.9gsbkTLAIbujYmFCgLdXX0b2KVM4DVuxG2ZDRj31PsQ"
  }
}