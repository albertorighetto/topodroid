/* @file TopoDroidApp.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid application (consts and prefs)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDVersion;
import com.topodroid.utils.TDLocale;
import com.topodroid.utils.TDString;
// import com.topodroid.utils.TDStatus;
import com.topodroid.ui.MyHorizontalListView;
import com.topodroid.prefs.TDPrefActivity;
import com.topodroid.prefs.TDPrefHelper;
import com.topodroid.prefs.TDSetting;
import com.topodroid.packetX.MemoryOctet;
import com.topodroid.dev.Device;
import com.topodroid.dev.ConnectionState;
import com.topodroid.dev.TopoDroidComm;
import com.topodroid.dev.DataType;
import com.topodroid.dev.distox1.DistoXA3Comm;
import com.topodroid.dev.distox1.DeviceA3Info;
import com.topodroid.dev.distox2.DistoX310Comm;
import com.topodroid.dev.distox2.DeviceX310Info;
import com.topodroid.dev.distox2.DeviceX310Details;
import com.topodroid.dev.sap.SapComm;
import com.topodroid.dev.bric.BricComm;
import com.topodroid.dev.bric.BricInfoDialog;
import com.topodroid.dev.PairingRequest;
import com.topodroid.common.LegType;
import com.topodroid.common.ExtendType;
import com.topodroid.common.PlotType;
// import com.topodroid.calib.CalibCoeffDialog;
// import com.topodroid.calib.CalibReadTask;
import com.topodroid.calib.CalibInfo;
import com.topodroid.calib.CalibExport;


import java.io.File; // PRIVATE FILE firmware-bin
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.StringReader;

import java.io.InputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
// import java.util.Stack;

// import android.os.Environment;
// import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
// import android.os.Debug;
// import android.os.SystemClock; // FIXME TROBOT

import android.database.sqlite.SQLiteDatabase;

import android.app.Application;
// import android.app.Notification;
// import android.app.NotificationManager;
// import android.app.KeyguardManager;
// import android.app.KeyguardManager.KeyguardLock;
// import android.app.Activity;

import android.content.Context;
// import android.content.Intent;
// import android.content.ActivityNotFoundException;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.Configuration;

import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
// import android.content.FileProvider;

import android.net.Uri;

// import android.provider.Settings;
// import android.provider.Settings.System;
// import android.provider.Settings.SettingNotFoundException;

// import android.view.WindowManager;
// import android.view.Display;
import android.view.ViewGroup.LayoutParams;

import android.widget.Button;
// import android.graphics.Point;
// import android.graphics.Bitmap;
// import android.graphics.BitmapFactory;
// import android.graphics.drawable.BitmapDrawable;

import android.util.DisplayMetrics;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;
// import android.bluetooth.BluetoothDevice; // COSURVEY

public class TopoDroidApp extends Application
{
  // static final String EMPTY = "";
  static private TopoDroidApp thisApp = null;

  /** check if the app has the database helper
   * @return true if the app has the database helper
   */
  static boolean hasTopoDroidDatabase() 
  { 
    // return ( TDFile.getTopoDroidFile( TDPath.getDatabase() ).exists() );
    return mData != null && mData.hasDB();
  }

  boolean mWelcomeScreen;  // whether to show the welcome screen (used by MainWindow)
  boolean mSetupScreen;    // whether to show the welcome screen (used by MainWindow)
  boolean mHasTDX;         // whether there is TDX folder
  static boolean mCheckManualTranslation = false;
  // static String mManual;  // manual url

  // static int mCheckPerms;

  static String mClipboardText = null; // text clipboard

  public static float mScaleFactor   = 1.0f; // DisplayHeight / 320 * density, eg, 2040/320*3.0 = 19.15...
  public static float mDisplayWidth  = 200f;
  public static float mDisplayHeight = 320f;
  public static float mBorderRight      = 4096;
  public static float mBorderLeft       = 0;
  public static float mBorderInnerRight = 4096;
  public static float mBorderInnerLeft  = 0;
  public static float mBorderTop        = 0;
  public static float mBorderBottom     = 4096; // in DrawingWindow

  // static boolean isTracing = false;

  /* FIXME_HIGHLIGHT
  private List< DBlock > mHighlighted = null;
  void    setHighlighted( List< DBlock > blks ) { mHighlighted = blks; }
  // int     getHighlightedSize() { return (mHighlighted != null)? -1 : mHighlighted.size(); }
  boolean hasHighlighted() { return mHighlighted != null && mHighlighted.size() > 0; }
  boolean hasHighlightedId( long id )
  {
    if ( mHighlighted == null ) return false;
    for ( DBlock b : mHighlighted ) if ( b.mId == id ) return true;
    return false;
  }
  */

  // cross-section splay display mode
  int mSplayMode = 2; 
  boolean mShowSectionSplays = true;
  

  // ----------------------------------------------------------------------
  // data lister
  // ListerSet mListerSet;
  ListerSetHandler mListerSet; // FIXME_LISTER

  /** register a lister in the lister-set
   * @param lister   data lister to register
   */
  void registerLister( ILister lister ) { if ( lister != null ) mListerSet.registerLister( lister ); }

  /** unregister a lister from the lister-set
   * @param lister   data lister to unregister
   */
  void unregisterLister( ILister lister ) { if ( lister != null ) mListerSet.unregisterLister( lister ); }

  /** notify a new status to the data listers
   * @param status new status
   */
  public void notifyStatus( final int status )
  { 
    // TDLog.v( "App: notify status " + status );
    if ( mMainActivity == null ) return;
    mMainActivity.runOnUiThread( new Runnable() { 
      public void run () { 
        mListerSet.setConnectionStatus( status );
      }
    } );
  }

  /** receive a disconnection notification
   * @param data_type   type of data that are downloaded
   */
  public void notifyDisconnected( int data_type )
  {
    if ( mListerSet.size() > 0 ) {
      try {
        new ReconnectTask( mDataDownloader, data_type, 500 ).execute();
      } catch ( RuntimeException e ) {
        TDLog.Error("reconnect error: " + e.getMessage() );
      }
    }
  }

  // -----------------------------------------------------

  // FIXME INSTALL_SYMBOL boolean askSymbolUpdate = false; // by default do not ask

  String[] DistoXConnectionError;
  // BluetoothAdapter mBTAdapter = null;     // BT connection
  private TopoDroidComm mComm = null;     // BT communication
  public DataDownloader mDataDownloader = null;  // data downloader
  public static DataHelper mData = null;         // database 
  public static DeviceHelper mDData = null;      // device/calib database

  // public static TDPrefHelper mPrefHlp      = null;
  static SurveyWindow mSurveyWindow    = null; // FIXME ref Survey Activity
  public static ShotWindow mShotWindow = null; // FIXME ref Shot Activity - public for prefs/TDSetting
  public static DrawingWindow mDrawingWindow  = null; // FIXME currently not used
  public static MainWindow mMainActivity      = null; // FIXME ref Main Activity

  /** @return the ID of the last shot
   */
  static long lastShotId( ) { return mData.getLastShotId( TDInstance.sid ); }

  static StationName mStationName = null;

  // static Device mDevice = null;
  // static int deviceType() { return (mDevice == null)? 0 : mDevice.mType; }
  // static String distoAddress() { return (mDevice == null)? null : mDevice.getAddress(); }

  // FIXME VirtualDistoX
  // VirtualDistoX mVirtualDistoX = new VirtualDistoX();

  /** notify the drawing window to update the midline display
   * @param blk_id    ID of the shot DBlock 
   * @param got_leg   whether a new leg has been detected
   */
  static void notifyDrawingUpdateDisplay( long blk_id, boolean got_leg )
  {
    if ( mDrawingWindow != null ) mDrawingWindow.notifyUpdateDisplay( blk_id, got_leg );
  }

  /** set the toolbar parameters of the drawing window
   */
  public static void setToolsToolbarParams()
  {
    if ( mDrawingWindow != null ) mDrawingWindow.setToolsToolbarParams();
  }

  // -------------------------------------------------------------------------------------
  // static SIZE methods

  /** @return the density of the display, e.g, 3.0
   */
  public static float getDisplayDensity( )
  {
    return Resources.getSystem().getDisplayMetrics().density;
  }

  // /** @return the densiti of the display
  //  * @param context   context (unused)
  //  */
  // public static float getDisplayDensity( Context context )
  // {
  //   return Resources.getSystem().getDisplayMetrics().density;
  // }

  /** @return the density DPI of the display, e.g., 480 // FIXED_ZOOM
   */
  public static int getDisplayDensityDpi( )
  {
    return Resources.getSystem().getDisplayMetrics().densityDpi;
  }

  /** set the height of the button list-view
   * @param context   context
   * @param listView  list view
   * @return button size
   */
  public static int setListViewHeight( Context context, MyHorizontalListView listView )
  {
    // int size = getScaledSize( context );
    if ( listView != null ) {
      LayoutParams params = listView.getLayoutParams();
      params.height = TDSetting.mSizeButtons + 10;
      listView.setLayoutParams( params );
    }
    return TDSetting.mSizeButtons;
  }

  /** @return the scaled size of buttons
   * @param context   context
   * @note default button size - USED by Tdm...
   */
  static int getScaledSize( Context context )
  {
    return (int)( TDSetting.mSizeButtons * context.getResources().getSystem().getDisplayMetrics().density );
  }
  
  /** re-set the button bar of the main window
   */
  public static void resetButtonBar() 
  { 
    if ( mMainActivity == null ) return;
    mMainActivity.runOnUiThread( new Runnable() { 
      public void run () { 
        mMainActivity.resetButtonBar(); 
      }
    } );
  }
   
  /** set the screen orientation
   */
  public static void setScreenOrientation() 
  { 
    if ( mMainActivity == null ) return;
    TDandroid.setScreenOrientation( mMainActivity  );
  }

  // /** re-set the menu adapter of the main window
  //  */
  // public static void setMenuAdapter( ) 
  // { 
  //   if ( mMainActivity == null ) return;
  //   mMainActivity.runOnUiThread( new Runnable() { 
  //     public void run () { 
  //       mMainActivity.setMenuAdapter( TDInstance.getResources() ); 
  //     }
  //   } );
  // }

  // UNUSED was called by HelpEntry
  // static int getDefaultSize( Context context )
  // {
  //   return (int)( 42 * context.getResources().getSystem().getDisplayMetrics().density );
  // }

  // ------------------------------------------------------------
  // CONSTS
  // private static final byte char0C = 0x0c;

  // ---------------------------------------------------------------
  // ConnListener
  ArrayList< Handler > mConnListener = null;

  /** register a connection-listener in the conn-listener set
   * @param hdl   connection listener
   */
  void registerConnListener( Handler hdl )
  {
    if ( hdl != null && mConnListener != null ) {
      mConnListener.add( hdl );
      // try { new Messenger( hdl ).send( new Message() ); } catch ( RemoteException e ) { }
    }
  }

  /** unregister a connection-listener from the conn-listener set
   * @param hdl   connection listener
   */
  void unregisterConnListener( Handler hdl )
  {
    if ( hdl != null && mConnListener != null ) {
      // try { new Messenger( hdl ).send( new Message() ); } catch ( RemoteException e ) { }
      mConnListener.remove( hdl );
    }
  }

  /** send a notification to the commention listeners
   * @param w  notification (value)
   */
  private void notifyConnState( int w )
  {
    // TDLog.v( "notify conn state" );
    if ( mConnListener == null ) return;
    for ( Handler hdl : mConnListener ) {
      try {
        Message msg = Message.obtain();
        msg.what = w;
        new Messenger( hdl ).send( msg );
      } catch ( RemoteException e ) { }
    }
  }
  
  // ---------------------------------------------------------------
  // survey/calib info
  //

  /** @return true if the calibration matches with the current device
   */
  boolean checkCalibrationDeviceMatch() 
  {
    CalibInfo info = mDData.selectCalibInfo( TDInstance.cid  );
    // TDLog.Log( TDLog.LOG_CALIB, "info.device " + ((info == null)? "null" : info.device) );
    // TDLog.Log( TDLog.LOG_CALIB, "device " + ((mDevice == null)? "null" : mDevice.getAddress()) );
    return ( TDInstance.getDeviceA() == null || ( info != null && info.device.equals( TDInstance.deviceAddress() ) ) );
  }

  /** @return the list of the survey names
   */ 
  public static List< String > getSurveyNames()
  {
    if ( mData == null ) return null;
    return mData.selectAllSurveys();
  }

  /** @return the info of the current survey
   */
  public static SurveyInfo getSurveyInfo()
  {
    if ( TDInstance.sid <= 0 ) return null;
    if ( mData == null ) return null;
    return mData.selectSurveyInfo( TDInstance.sid );
    // if ( info == null ) TDLog.Error("null survey info. sid " + TDInstance.sid );
  }

  /** @return the xsection mode of the current survey: 0 shared, 1 private, -1 error
   */
  public static int getSurveyXSectionsMode() 
  {
    if ( TDInstance.sid <= 0 ) return -1;
    if ( mData == null ) return -1;
    return mData.getSurveyXSectionsMode( TDInstance.sid );
  }

  /** @return the "extend" value of the current survey (or default to NORMAL)
   */
  private static int getSurveyExtend()
  {
    if ( TDInstance.sid <= 0 ) return SurveyInfo.SURVEY_EXTEND_NORMAL;
    if ( mData == null ) return SurveyInfo.SURVEY_EXTEND_NORMAL;
    return mData.getSurveyExtend( TDInstance.sid );
  }

  /** set the "extend" value of the current survey
   * @param extend   extend value
   */
  public static void setSurveyExtend( int extend )
  {
    // TDLog.v( "set Survey Extend: " + extend );
    if ( TDInstance.sid <= 0 ) return;
    if ( mData == null ) return;
    mData.updateSurveyExtend( TDInstance.sid, extend );
  }

  /** @return the info of the current calibration
   */
  public CalibInfo getCalibInfo()
  {
    if ( TDInstance.cid <= 0 ) return null;
    if ( mDData == null ) return null;
    return mDData.selectCalibInfo( TDInstance.cid );
  }

  /** @return the names of the stations of the current survey
   */
  Set<String> getStationNames() { return mData.selectAllStations( TDInstance.sid ); }

  // ----------------------------------------------------------------

  @Override 
  public void onTerminate()
  {
    super.onTerminate();
    thisApp = null;
  }

  /** set the model of the bluetooth device
   * @param device   bluetooth device (must agree with the primary device)
   * @param model    device model
   *
   * @note nothing if the device is not the primary device
   */
  static void setDeviceModel( Device device, int model )
  {
    if ( device != null && device == TDInstance.getDeviceA() ) {
      if ( device.mType != model ) {
        if ( Device.isA3( model ) ) {
          mDData.updateDeviceModel( device.getAddress(), "DistoX" );
          device.mType = model;
        } else if ( Device.isX310( model ) ) {
          mDData.updateDeviceModel( device.getAddress(), "DistoX-0000" );
          device.mType = model;
        } else if ( Device.isSap( model ) ) {
          mDData.updateDeviceModel( device.getAddress(), "Shetland-0000" );
          device.mType = model;
        } else if ( Device.isBric( model ) ) {
          mDData.updateDeviceModel( device.getAddress(), "BRIC-0000" );
          device.mType = model;
        // } else if ( Device.isX000( model ) ) { // FIXME VirtualDistoX
        //   mDData.updateDeviceModel( device.getAddress(), "DistoX0" );
        //   device.mType = model;
        }
      }
    }
  }

  /** set the blluetooth device nickname
   * @param device      device
   * @param nickname    device nickmane
   */
  static void setDeviceName( Device device, String nickname )
  {
    if ( device != null /* && device == TDInstance.getDeviceA() */ ) {
      mDData.updateDeviceNickname( device.getAddress(), nickname );
      device.mNickname = nickname;
    }
  }

  // called by DeviceActivity::onResume()
  public void resumeComm() { if ( mComm != null ) mComm.resume(); }

  public void suspendComm() { if ( mComm != null ) mComm.suspend(); }

  public void resetComm() 
  { 
    createComm();
    mDataDownloader.onStop(); // mDownload = false;
  }

  // FIXME BT_RECEIVER 
  // void resetCommBTReceiver()
  // {
  //   mComm.resetBTReceiver();
  // }

  // called by DeviceActivity::setState()
  //           ShotWindow::onResume()
  public boolean isCommConnected()
  {
    // return mComm != null && mComm.mBTConnected;
    // return mComm != null && mComm.mBTConnected && mComm.mCommThread != null;
    return mComm != null && mComm.isConnected() && ! mComm.isCommThreadNull(); // FIXME BLE5 to check
  }

  void disconnectRemoteDevice( boolean force )
  {
    // TDLog.v( "App: disconnect remote device. force " + force );
    // TDLog.Log( TDLog.LOG_COMM, "App disconnect RemoteDevice listers " + mListerSet.size() + " force " + force );
    if ( force || mListerSet.size() == 0 ) {
      if ( mComm != null && mComm.isConnected() ) {
        mComm.disconnectRemoteDevice( ); // FIXME BLE5 to check
      }
    }
  }

  private void deleteComm() // FIXME BLE5
  {
    // TDLog.v( "App: delete comm");
    if ( mComm != null ) {
      if ( mComm.isConnected() ) {
        mComm.disconnectRemoteDevice(); 
      }
      mComm = null;
    }
  }

  // void connectRemoteDevice( String address )
  // {
  //   if ( mComm != null ) mComm.connectRemoteDevice( address, mListerSet );
  // }

  // FIXME_COMM
  // @param address   device address
  // @param data_type data type ...
  public boolean connectDevice( String address, int data_type ) 
  {
    // TDLog.v( "App: connect address " + address + " comm is " + ((mComm==null)? "null" : "non-null") );
    return mComm != null && mComm.connectDevice( address, mListerSet, data_type ); // FIXME_LISTER
  }

  public boolean disconnectComm()
  {
    // TDLog.v( "App: disconnect. comm is " + ((mComm==null)? "null" : "non-null") );
    return ( mComm == null ) || mComm.disconnectDevice();
  }

  public void notifyConnectionState( int state )
  {
    // TODO
    // TDLog.v( "App: notify conn state " + state + " TODO" );
  }
  // end FIXME_COMM

  // --------------------------------------------------------------------------------------

  public DeviceA3Info readDeviceA3Info( String address )
  {
    DeviceA3Info info = new DeviceA3Info();
    byte[] ret = readMemory( address, 0x8008 );
    if ( ret == null ) return null;
    info.mCode = String.format( getResources().getString( R.string.device_code ), MemoryOctet.toInt( ret[1], ret[0] ) );

    ret = readMemory( TDInstance.deviceAddress(), 0x8000 );
    if ( ret == null ) return null;

    info.mAngle   = getResources().getString( 
                    (( ret[0] & 0x01 ) != 0)? R.string.device_status_angle_grad : R.string.device_status_angle_degree );
    info.mCompass = getResources().getString( 
                    (( ret[0] & 0x04 ) != 0)? R.string.device_status_compass_on : R.string.device_status_compass_off );
    info.mCalib   = getResources().getString(
                    (( ret[0] & 0x08 ) != 0)? R.string.device_status_calib : R.string.device_status_normal );
    info.mSilent  = getResources().getString(
                    (( ret[0] & 0x10 ) != 0)? R.string.device_status_silent_on : R.string.device_status_silent_off );
    resetComm();
    return info;
  }

  public DeviceX310Info readDeviceX310Info( String address )
  {
    // TDLog.v( "read info - address " + address );
    DeviceX310Info info = new DeviceX310Info();
    byte[] ret = readMemory( address, 0x8008 );
    if ( ret == null ) {
      TDLog.Error("Failed read 8008");
      // TDLog.v( "read 8008 failed");
      return null;
    }
    // TDLog.Log( TDLog.LOG_COMM, "Addr 8008 (code): " + ret[0] + " " + ret[1] );
    // TDLog.v( "read 8008 (code): " + ret[0] + " " + ret[1] );
    info.mCode = String.format( getResources().getString( R.string.device_code ), MemoryOctet.toInt( ret[1], ret[0] ) );

    // ret = readMemory( address, 0xe000 );
    ret = readMemory( address, DeviceX310Details.FIRMWARE_ADDRESS );
    if ( ret == null ) {
      TDLog.Error("Failed read e000" );
      return null;
    }
    // TDLog.Log( TDLog.LOG_COMM, "Addr e000 (fw): " + ret[0] + " " + ret[1] );
    info.mFirmware = String.format( getResources().getString( R.string.device_firmware ), ret[0], ret[1] );
    // int fw0 = ret[0]; // this is always 2
    int fw1 = ret[1]; // firmware 2.X

    // ret = readMemory( address, 0xe004 );
    ret = readMemory( address, DeviceX310Details.HARDWARE_ADDRESS );
    if ( ret == null ) {
      TDLog.Error("Failed read e004" );
      return null;
    }
    // TDLog.Log( TDLog.LOG_COMM, "Addr e004 (hw): " + ret[0] + " " + ret[1] );
    info.mHardware = String.format( getResources().getString( R.string.device_hardware ), ret[0], ret[1] );

    // ret = readMemory( address, 0xc044 );
    // if ( ret != null ) {
    //   // TDLog.v( "X310 info C044 " + String.format( getResources().getString( R.string.device_memory ), ret[0], ret[1] ) );
    // }

    resetComm();
    return info;
  }

  /** read head-tail from a DistoX A3
   * @param address    device address
   * @param command
   * @param head_tail  return array with positions of head and tail
   * @return head-tail string, null on failure
   */
  public String readA3HeadTail( String address, byte[] command, int[] head_tail )
  {
    if ( mComm instanceof DistoXA3Comm ) {
      DistoXA3Comm comm = (DistoXA3Comm)mComm;
      String ret = comm.readA3HeadTail( address, command, head_tail );
      resetComm();
      return ret;
    } else {
      TDLog.Error("read A3 HeadTail: class cast exception");
    }
    return null;
  }

  /** swap hot bit in the range [from, to)
   * @param address   device address
   * @param from      initial memory address
   * @param to        final memory address (non-inclusive)
   * @param on_off    whether to swap hot bit ON or OFF
   * @return the number of bit that have been swapped
   */
  public int swapA3HotBit( String address, int from, int to, boolean on_off ) 
  {
    if ( mComm instanceof DistoXA3Comm ) {
      DistoXA3Comm comm = (DistoXA3Comm)mComm;
      int ret = comm.swapA3HotBit( address, from, to, on_off ); // FIXME_A3
      resetComm();
      return ret;
    } else {
      TDLog.Error("swap A3 HeadTail: not A3 comm");
    }
    return -1; // failure
  }

  static boolean mEnableZip = true;  // whether zip saving is enabled or must wait (locked by th2. saving thread)
  static boolean mSketches = false;  // whether to use 3D models

  // ---------------------------------------------------------

  /** second step of application initialization
   */
  void startupStep2( )
  {
    TDPrefHelper prefHlp = new TDPrefHelper( this );
    // ***** LOG FRAMEWORK
    TDLog.loadLogPreferences( prefHlp );
    // TDLog.v( "log load prefs done");

    // mData.compileStatements(); // this method is now empty (and commented)

    PtCmapActivity.setMap( prefHlp.getString( "DISTOX_PT_CMAP", null ) );
    TDLog.v( "PCmap set map done");

    TDSetting.loadSecondaryPreferences( prefHlp );
    TDLog.v( "load secondary done");
    checkAutoPairing();

    // if ( TDLog.LOG_DEBUG ) {
    //   isTracing = true;
    //   Debug.startMethodTracing("DISTOX");
    // }

    // TDLog.Debug("ready");
  }

  /** create the bluetooth communication
   */
  private void createComm()
  {
    if ( mComm != null ) {
      mComm.disconnectRemoteDevice( );
      mComm = null;
    }
    // if ( TDInstance.isDeviceAddress( Device.ZERO_ADDRESS ) ) { // FIXME VirtualDistoX
    //   mComm = new VirtualDistoXComm( this, mVirtualDistoX );
    // } else {
      if ( TDInstance.isDeviceX310() ) {
        // TDLog.v( "App: create DistoX2 comm");
        mComm = new DistoX310Comm( this );
      } else if ( TDInstance.isDeviceA3() ) {
        // TDLog.v( "App: create DistoX1 comm");
        mComm = new DistoXA3Comm( this );
      } else if ( TDInstance.isDeviceSap() ) {
        String address = TDInstance.deviceAddress();
        BluetoothDevice bt_device = TDInstance.getBleDevice();
        // TDLog.v( "App: create SAP comm");
        mComm = new SapComm( this, address, bt_device );
      } else if ( TDInstance.isDeviceBric() ) {
        String address = TDInstance.deviceAddress();
        BluetoothDevice bt_device = TDInstance.getBleDevice();
        // TDLog.v( "App: create BRIC comm");
        mComm = new BricComm( this, this, address, bt_device );
      // } else if ( TDInstance.isDeviceBlex() ) {
      //   address = TDInstance.deviceAddress();
      //   BluetoothDevice bt_device = TDInstance.getBleDevice();
      //   // TDLog.v( "App: create ble comm. address " + address + " BT " + ((bt_device==null)? "null" : bt_device.getAddress() ) );
      //   mComm = new BleComm( this, address, bt_device );
      }
    // }
  }

  /** react to a user tap on the bluetooth button
   * @param ctx       context
   * @param lister    data lister
   * @param b         bluetooth button
   * @param nr_shots  ...
   */
  void doBluetoothButton( Context ctx, ILister lister, Button b, int nr_shots )
  {
    if ( TDLevel.overAdvanced ) {
      if ( TDInstance.isDeviceBric() ) {
        // TDLog.v( "bt button over advanced : BRIC");
        if ( mComm != null && mComm.isConnected() ) { // FIXME BRIC_TESTER
          CutNPaste.showPopupBT( ctx, lister, this, b, false, (nr_shots == 0) );
          return;
        }
      } else if ( TDInstance.isDeviceSap() ) { // SAP5
        // TDLog.v( "bt button over advanced : SAP");
        /* nothing */
      } else { // DistoX
        // TDLog.v( "bt button over advanced : DistoX");
        if ( ! mDataDownloader.isDownloading() ) {
          if ( TDInstance.hasDeviceRemoteControl() && ! TDSetting.isConnectionModeMulti()) {
            CutNPaste.showPopupBT( ctx, lister, this, b, false, (nr_shots == 0) );
            return;
          }
        } else { // downloading: nothing
          return;
        }
      }
    }
    // TDLog.v( "bt button not over advanced");
    doBluetoothReset( lister );
  }

  /** reset the bluetooth
   * @param lister  data lister
   */
  private void doBluetoothReset( ILister lister )
  {
    mDataDownloader.setDownload( false );
    mDataDownloader.stopDownloadData();
    lister.setConnectionStatus( mDataDownloader.getStatus() );
    this.resetComm();
    TDToast.make(R.string.bt_reset );
  }
 
  /** initualize the display parameters 
   * @param dm     display metrics
   * // @param landscape whether the screen orientation is landscape
   */
  static private void setDisplayParams( DisplayMetrics dm /* , boolean landscape */ )
  {
    float density  = dm.density;
    float dim = dm.widthPixels;
    mBorderRight      = dim * 15 / 16;
    mBorderLeft       = dim / 16;
    mBorderInnerRight = dim * 3 / 4;
    mBorderInnerLeft  = dim / 4;
    mBorderTop        = dm.heightPixels / 8;
    mBorderBottom     = (dm.heightPixels * 7) / 8 + DrawingWindow.ZOOM_TRANSLATION_1;
    mDisplayWidth  = dm.widthPixels;
    mDisplayHeight = dm.heightPixels;
    // TDLog.v("ConfigChange set display params " + mDisplayWidth + " " + mDisplayHeight + " landscape " + landscape + " dim " + dim );
  }

  @Override
  public void onCreate()
  {
    super.onCreate();

    thisApp = this;
    TDInstance.setContext( getApplicationContext() );

    // MODE_WORLD_WRITEABLE and MODE_WORLD_READABLE are no longer supported
    // SQLiteDatabase dbase = openOrCreateDatabase("DISTOX14", 0, null );
    // DBase path: /data/user/0/com.topodroid.DistoX/databases/DISTOX14

    // ONLY IF BUILT WITH ANDROID-30 and above
    // if ( TDandroid.ABOVE_API_29 ) {
    //   if ( ! android.os.Environment.isExternalStorageManager() ) {
    //     // Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID );
    //     // startActivity( android.content.Intent( android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri ) );
    //     android.content.Intent intent = new android.content.Intent();
    //     intent.setAction( android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION );
    //     startActivity( intent );
    //   }
    // }

    // require large memory pre Honeycomb
    // dalvik.system.VMRuntime.getRuntime().setMinimumHeapSize( 64<<20 );

    // TDLog.Profile("TDApp onCreate");
    boolean hasTopoDroidDir = TDFile.getTopoDroidFile( "/sdcard/Documents/TopoDroid" ).exists();
    boolean hasTDXDir =  TDFile.hasExternalDir(null);
    TDLog.v( "APP has external dir: " + hasTDXDir + " " + TDFile.hasExternalDir("TopoDroid") );
    TDLog.v( "APP has old TopoDroid dir: " + hasTopoDroidDir );
    if ( hasTopoDroidDir && ! hasTDXDir ) {
      TDLog.v("APP move to 6");
      TDPath.moveTo6( this );
    }

    TDVersion.setVersion( this );
    TDPrefHelper prefHlp = new TDPrefHelper( this );

    mWelcomeScreen = prefHlp.getBoolean( "DISTOX_WELCOME_SCREEN", true ); // default: WelcomeScreen = true
    // if ( mWelcomeScreen ) {
    //   setDefaultSocketType( );
    // }
    mSetupScreen = prefHlp.getBoolean( "DISTOX_SETUP_SCREEN", true ); // default: SetupScreen = true
    if ( mSetupScreen ) { // deafult button size
      TDSetting.mSizeBtns = 5;
      TDSetting.mSizeButtons =  (int)( TDSetting.BTN_SIZE_UNUSED * TopoDroidApp.getDisplayDensity() * 0.86f );
    }

    DistoXConnectionError = new String[5];
    DistoXConnectionError[0] = getResources().getString( R.string.distox_err_ok );
    DistoXConnectionError[1] = getResources().getString( R.string.distox_err_headtail );
    DistoXConnectionError[2] = getResources().getString( R.string.distox_err_headtail_io );
    DistoXConnectionError[3] = getResources().getString( R.string.distox_err_headtail_eof );
    DistoXConnectionError[4] = getResources().getString( R.string.distox_err_connected );

    DisplayMetrics dm = getResources().getDisplayMetrics();
    setDisplayParams( dm /* , getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE */ );

    mScaleFactor   = (mDisplayHeight / 320.0f) * dm.density;
    // FIXME it would be nice to have this, but it breaks all existing sketches
    //       therefore must stick with initial choice
    // DrawingUtil.CENTER_X = mDisplayWidth  / 2;
    // DrawingUtil.CENTER_Y = mDisplayHeight / 2;

    mListerSet = new ListerSetHandler();
    mEnableZip = true;

    // initEnvironmentFirst( prefHlp ); // must be called after the permissions

    // mCheckPerms = TDandroid.checkPermissions( this );
    // if ( mCheckPerms >= 0 ) {
    //   initEnvironmentSecond( ); // called by MainWindow.onStart
    // }

    // mManual = getResources().getString( R.string.topodroid_man );

    // TDLog.v( "W " + mDisplayWidth + " H " + mDisplayHeight + " D " + density );

  }

  static boolean done_init_env_second = false;

  /** second step of environment initailization
   * @param with_dialog_r
   * @return true if successful - can fail if cannot open the database
   */
  static  boolean initEnvironmentSecond( boolean with_dialog_r )
  {
    if ( done_init_env_second ) return true;
    done_init_env_second = true;
    // TDLog.v("APP init env. second " );

    TDPrefHelper prefHlp = new TDPrefHelper( thisApp );

    // TDLog.Profile("TDApp cwd");
    TDInstance.cwd = prefHlp.getString( "DISTOX_CWD", "TopoDroid" );
    TDInstance.cbd = TDPath.getCurrentBaseDir();

    // TDLog.Profile("TDApp paths");
    TDPath.setTdPaths( TDInstance.cwd /*, TDInstance.cbd */ );

    // TDLog.Profile("TDApp DB"); 
    // ***** DATABASE MUST COME BEFORE PREFERENCES
    // if ( ! with_dialog_r ) {
      // TDLog.v( "Open TopoDroid Database");
      mData = new DataHelper( thisApp ); 
    // }

    // mStationName = new StationName();
    return mData.hasDB();
  }

  // init env requires the device database, which requires having the permissions
  //
  static boolean done_init_env_first = false;

  /** initialize the environment, first step
   */
  void initEnvironmentFirst(  ) // TDPrefHelper prefHlp 
  {
    if ( done_init_env_first ) return;
    done_init_env_first = true;

    // TDLog.v( "APP init env. first " );
    TDPrefHelper prefHlp = new TDPrefHelper( this );
    mDData = new DeviceHelper( thisApp );
    // TDLog.Profile("TDApp prefs");
    // LOADING THE SETTINGS IS RATHER EXPENSIVE !!!
    TDSetting.loadPrimaryPreferences( TDInstance.getResources(),  prefHlp );

    thisApp.mDataDownloader = new DataDownloader( thisApp, thisApp );

    // ***** DRAWING TOOLS SYMBOLS
    // TDLog.Profile("TDApp symbols");

    // if one of the symbol dirs does not exists all of then are restored
    String version = mDData.getValue( "version" );
    // TDLog.v("PATH " + "version " + version + " " + TDVersion.string() );
    // TDLog.v( "DData version <" + version + "> TDversion <" + TDVersion.string() + ">" );
    if ( version == null || ( ! version.equals( TDVersion.string() ) ) ) {
      mDData.setValue( "version",  TDVersion.string()  );
      // FIXME INSTALL_SYMBOL installSymbols( false ); // this updates symbol_version in the database
      if ( mDData.getValue( "symbol_version" ) == null ) {
        // TDLog.v("PATH " + "symbol version " + symbol_version );
        installSymbols( true );
      }
      installFirmware( false );
      // installUserManual( );
      mCheckManualTranslation = true;
    }

    // ***** CHECK SPECIAL EXPERIMENTAL FEATURES : SKETCH
    // if ( TDLevel.overTester ) {
    //   String value = mDData.getValue("sketches");
    //   mSketches =  value != null 
    //           && value.equals("on")
    //           && getPackageManager().hasSystemFeature( PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH );
    // }

    /* ---- IF_COSURVEY
    if ( TDLevel.overExpert ) {
      String value = mDData.getValue("cosurvey");
      mCosurvey =  value != null && value.equals("on");
      setCoSurvey( false );
      prefHlp.update( "DISTOX_COSURVEY", false );
      if ( mCosurvey ) {
        mSyncConn = new ConnectionHandler( this );
        mConnListener = new ArrayList<>();
      }
    }
    */

    // TDLog.Profile("TDApp device etc.");
    TDInstance.setDeviceA( mDData.getDevice( prefHlp.getString( TDSetting.keyDeviceName(), TDString.EMPTY ) ) );

    if ( TDInstance.getDeviceA() != null ) {
      thisApp.createComm();
    }
  }

  /** attach base context
   * @param ctx   context
   */
  @Override
  protected void attachBaseContext( Context ctx )
  {
    TDLog.v("APP attach base context");
    TDInstance.context = ctx;
    TDLocale.resetTheLocale();
    super.attachBaseContext( TDInstance.context );
  }

  /** react to a change of configuration
   * @param cfg  new configuration
   */
  @Override
  public void onConfigurationChanged( Configuration cfg )
  {
    super.onConfigurationChanged( cfg );
    // boolean landscape = cfg.orientation == Configuration.ORIENTATION_LANDSCAPE;
    TDLog.v("APP config change" );
    TDLocale.resetTheLocale( );
    setDisplayParams( getResources().getDisplayMetrics() /* , landscape */ );
  }

  /** set the current work directoty
   * @param cwd current work directory
   * @param cbd current base directory (UNUSED)
   */
  public static void setCWD( String cwd /* , String cbd */ )
  {
    if ( cwd == null || cwd.length() == 0 ) cwd = TDInstance.cwd;
    // TDLog.v( "App set CWD " + cwd + " CBD " + cbd );

    if ( cwd.equals( TDInstance.cwd ) ) return;
    // TDInstance.cbd = cbd;
    TDInstance.cwd = cwd;
    TDLog.Log( TDLog.LOG_PATH, "App set cwd <" + cwd + /* "> cbd <" + cbd + */ ">");
    mData.closeDatabase();

    TDPath.setTdPaths( TDInstance.cwd /*, TDInstance.cbd */ );
    mData.openDatabase( TDInstance.context );

    if ( mMainActivity != null ) mMainActivity.setTheTitle( );
  }

// -----------------------------------------------------------------

  /** upload the calibration coefficients to the DistoX
   * @param coeff    calibration coefficients
   * @param check    whether to check that the calibration matches the device 
   * @param b        "write" button - to update its state at the end
   * @note called by GMActivity and by CalibCoeffDialog and DeviceActivity (to reset coeffs)
   */
  public void uploadCalibCoeff( byte[] coeff, boolean check, Button b )
  {
    // TODO this writeCoeff shoudl be run in an AsyncTask
    if ( b != null ) b.setEnabled( false );
    if ( mComm == null || TDInstance.getDeviceA() == null ) {
      TDToast.makeBad( R.string.no_device_address );
    } else if ( check && ! checkCalibrationDeviceMatch() ) {
      TDToast.makeBad( R.string.calib_device_mismatch );
    } else if ( ! mComm.writeCoeff( TDInstance.deviceAddress(), coeff ) ) {
      TDToast.makeBad( R.string.write_failed );
    } else {
      TDToast.make( R.string.write_ok );
    }
    if ( b != null ) b.setEnabled( true );
    resetComm();
  }

  /** read the calibration coefficients from the DistoX
   * @param coeff    calibration coefficients [output]
   * @return true if successful
   * @note called by CalibReadTask.onPostExecute
   */
  public boolean readCalibCoeff( byte[] coeff )
  {
    if ( mComm == null || TDInstance.getDeviceA() == null ) return false;
    boolean ret = mComm.readCoeff( TDInstance.deviceAddress(), coeff );
    resetComm();
    return ret;
  }

  /** toggle the DistoX calibration/normal mode
   * @return true if successful
   * @note called by CalibToggleTask.doInBackground
   */
  public boolean toggleCalibMode( )
  {
    if ( mComm == null || TDInstance.getDeviceA() == null ) return false;
    boolean ret = mComm.toggleCalibMode( TDInstance.deviceAddress(), TDInstance.deviceType() );
    resetComm();
    return ret;
  }

  /** read the DistoX memory
   * @param address   device address
   * @param addr      memory address
   * @return array of bytes read from the DistoX memory at the given address
   */
  public byte[] readMemory( String address, int addr )
  {
    if ( mComm == null || isCommConnected() ) return null;
    byte[] ret = mComm.readMemory( address, addr );
    resetComm();
    return ret;
  }

  /** read the DistoX2 (X310) memory
   * @param address   device address
   * @param h0        ?
   * @param h1        ?
   * @param memory    array of octets to be filled by the memory-read
   * @return number of octets that have been read (-1 on error)
   */
  public int readX310Memory( String address, int h0, int h1, ArrayList< MemoryOctet > memory )
  {
    if ( mComm == null || isCommConnected() ) return -1;
    if ( mComm instanceof DistoX310Comm ) {
      DistoX310Comm comm = (DistoX310Comm)mComm;
      int ret = comm.readX310Memory( address, h0, h1, memory );
      resetComm();
      return ret;
    } else {
      TDLog.Error("read X310 memory: not X310 comm");
    }
    return -1;
  }

  /** read the DistoX (A3) memory
   * @param address   device address
   * @param h0        ?
   * @param h1        ?
   * @param memory    array of octets to be filled by the memory-read
   * @return number of octets that have been read (-1 on error)
   */
  public int readA3Memory( String address, int h0, int h1, ArrayList< MemoryOctet > memory )
  {
    if ( mComm == null || isCommConnected() ) return -1;
    if ( mComm instanceof DistoXA3Comm ) {
      DistoXA3Comm comm = (DistoXA3Comm)mComm;
      int ret = comm.readA3Memory( address, h0, h1, memory );
      resetComm();
      return ret;
    } else {
      TDLog.Error("read A3 memory: not A3 comm");
    }
    return -1;
  }

  // ------------------------------------------------------------------
  // FILE NAMES

  /** write the manifest file for the current survey
   */
  public void writeManifestFile()
  {
    SurveyInfo info = mData.selectSurveyInfo( TDInstance.sid );
    try {
      String filename = TDPath.getManifestFile();
      // TDPath.checkPath( filename );
      FileWriter fw = TDFile.getFileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );
      pw.format( "%s %d\n",  TDVersion.string(), TDVersion.code() );
      pw.format( "%s\n", TDVersion.DB_VERSION );
      pw.format( "%s\n", info.name );
      pw.format("%s\n", TDUtil.currentDate() );
      fw.flush();
      fw.close();
    } catch ( FileNotFoundException e ) {
      TDLog.Error("manifest write failure: no file");
    } catch ( IOException e ) {
      TDLog.Error("manifest write failure: " + e.getMessage() );
    }
  }

  static int mManifestDbVersion = 0;

  /** check a  manifest file
   * @param manifest   content of manifest 
   * @param surveyname survey name (@note surveyname is modified)
   * @return
   *  >=0 ok
   * -1 survey already present
   * -2 TopoDroid version mismatch
   * -3 database version mismatch: manifest_DB_version < min_DB_version
   * -4 database version mismatch: manifest_DB_version > current DB_version
   * -5 survey name does not match filename
   * -10 number format error
   * -11 file not found
   * -12 IO error
   */
  static public int checkManifestFile( String manifest, String surveyname )
  {
    mManifestDbVersion = 0;
    String line;
    // if ( mData.hasSurveyName( surveyname ) ) {
    //   return -1;
    // }
    int version_code = 0;
    int ret = -1;
    try {
      // FileReader fr = TDFile.getFileReader( filename );
      StringReader fr = new StringReader( manifest );
      BufferedReader br = new BufferedReader( fr );
      // first line is version
      line = br.readLine().trim();
      ret = checkVersionLine( line );
      if ( ret < 0 ) return ret;

      line = br.readLine().trim();
      try {
        mManifestDbVersion = Integer.parseInt( line );
      } catch ( NumberFormatException e ) {
        TDLog.Error( "parse error: db version " + line );
        return -10;
      }
      
      if ( ! ( mManifestDbVersion >= TDVersion.DATABASE_VERSION_MIN ) ) {
        TDLog.Error( "TopoDroid DB version mismatch: found " + mManifestDbVersion + " min " + + TDVersion.DATABASE_VERSION_MIN );
        return -3;
      }
      if ( ! ( mManifestDbVersion <= TDVersion.DATABASE_VERSION ) ) {
        TDLog.Error( "TopoDroid DB version mismatch: found " + mManifestDbVersion + " current " + TDVersion.DATABASE_VERSION );
        return -4;
      }
      surveyname = br.readLine().trim();
      // if ( ! line.equals( surveyname ) ) return -4;
      if ( mData.hasSurveyName( surveyname ) ) {
        TDLog.Error( "TopoDroid survey exists already: <" + surveyname + ">" );
        return -1;
      }
      // fr.close();
    } catch ( NumberFormatException e ) {
      TDLog.Error( "TopoDroid check manifest error: " + e.getMessage() );
      return -10;
    } catch ( FileNotFoundException e ) {
      TDLog.Error( "TopoDroid check manifest file not found: " + e.getMessage() );
      return -11;
    } catch ( IOException e ) {
      TDLog.Error( "TopoDroid check manifest I/O error: " + e.getMessage() );
      return -12;
    }
    return ret;
  }

  /** check the version line of a manifest file
   * @param version_line version line
   * @return
   *   -10 number format error
   *   -2  version is too old
   *   0   version is in acceptable range
   *   1   version is newer than this app
   */
  private static int checkVersionLine( String version_line )
  {
    int ret = 0;
    int version_code = 0;
    String[] vers = version_line.split(" ");
    for ( int k=1; k<vers.length; ++ k ) {
      if ( vers[k].length() > 0 ) {
        try {
          version_code = Integer.parseInt( vers[k] );
          break;
        } catch ( NumberFormatException e ) { 
          // this is OK
        }
      }
    }
    if ( version_code == 0 ) {
      String[] ver = vers[0].split("\\.");
      int major = 0;
      int minor = 0;
      int sub   = 0;
      char vch  = ' '; // char order: ' ' < A < B < ... < a < b < ... < '}' 
      if ( ver.length > 2 ) { // M.m.sv version code
        try {
          major = Integer.parseInt( ver[0] );
          minor = Integer.parseInt( ver[1] );
        } catch ( NumberFormatException e ) {
          TDLog.Error( "parse error: major/minor " + ver[0] + " " + ver[1] );
          return -10;
        }
        int k = 0;
        while ( k < ver[2].length() ) {
          char ch = ver[2].charAt(k);
          if ( ch < '0' || ch > '9' ) { vch = ch; break; }
          sub = 10 * sub + (int)(ch - '0');
          ++k;
        }
        // TDLog.v( "Version " + major + " " + minor + " " + sub );
        if (    ( major <  TDVersion.MAJOR_MIN )
             || ( major == TDVersion.MAJOR_MIN && minor < TDVersion.MINOR_MIN )
             || ( major == TDVersion.MAJOR_MIN && minor == TDVersion.MINOR_MIN && sub < TDVersion.SUB_MIN ) 
          ) {
          TDLog.Error( "TopoDroid version mismatch: " + version_line + " < " + TDVersion.MAJOR_MIN + "." + TDVersion.MINOR_MIN + "." + TDVersion.SUB_MIN );
          return -2;
        }
        if (    ( major > TDVersion.MAJOR ) 
             || ( major == TDVersion.MAJOR && minor > TDVersion.MINOR )
             || ( major == TDVersion.MAJOR && minor == TDVersion.MINOR && sub > TDVersion.SUB ) ) {
          ret = 1; 
        } else if ( major == TDVersion.MAJOR && minor == TDVersion.MINOR && sub == TDVersion.SUB && vch > ' ' ) {
          if ( TDVersion.VCH == ' ' ) { 
            ret = 1;
          } else if ( TDVersion.VCH <= 'Z' && ( vch >= 'a' || vch < TDVersion.VCH ) ) { // a-z or vch(A-Z) < VCH
            ret = 1;
          } else if ( TDVersion.VCH >= 'a' && vch < TDVersion.VCH ) { // A-Z < a-z 
            ret = 1;
          }
        }

      } else { // version code
        try {
          version_code = Integer.parseInt( ver[0] );
          if ( version_code < TDVersion.CODE_MIN ) {
            TDLog.Error( "TopoDroid version mismatch: " + version_line + " < " + TDVersion.CODE_MIN );
            return -2;
          }
        } catch ( NumberFormatException e ) {
          TDLog.Error( "parse error: version code " + ver[0] + " " + e.getMessage() );
          return -10;
        }
        if ( version_code > TDVersion.VERSION_CODE ) ret = 1;
      }
    } else {
      if ( version_code > TDVersion.VERSION_CODE ) ret = 1;
    }
    return ret;
  }

  // ----------------------------------------------------------
  // SURVEY AND CALIBRATION

  /** rename the survey
   * @param sid   survey ID
   * @param name  new survey name
   * @return true if success
   */
  boolean renameCurrentSurvey( long sid, String name )
  {
    if ( name == null || name.length() == 0 ) return false;
    if ( name.equals( TDInstance.survey ) ) return true;
    if ( mData == null ) return false;
    if ( mData.renameSurvey( sid, name ) ) {  
      { // rename plot/sketch files: th3
        List< PlotInfo > plots = mData.selectAllPlots( sid );
        for ( PlotInfo p : plots ) {
          // Tdr
          TDFile.renameFile( TDPath.getSurveyPlotTdrFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotTdrFile( name, p.name ) );
          // rename exported plots: th2 dxf png svg csx
          // TDFile.renameFile( TDPath.getSurveyPlotTh2File( TDInstance.survey, p.name ), TDPath.getSurveyPlotTh2File( name, p.name ) );
          // TDFile.renameFile( TDPath.getSurveyPlotDxfFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotDxfFile( name, p.name ) );
          // TDFile.renameFile( TDPath.getSurveyPlotShzFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotShzFile( name, p.name ) );
          // TDFile.renameFile( TDPath.getSurveyPlotSvgFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotSvgFile( name, p.name ) );
          // // TDFile.renameFile( TDPath.getSurveyPlotHtmFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotHtmFile( name, p.name ) );
          // TDFile.renameFile( TDPath.getSurveyPlotTnlFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotTnlFile( name, p.name ) );
          // TDFile.renameFile( TDPath.getSurveyPlotPngFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotPngFile( name, p.name ) );
          // TDFile.renameFile( TDPath.getSurveyPlotCsxFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotCsxFile( name, p.name ) );
          // TDFile.renameFile( TDPath.getSurveyPlotXviFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotXviFile( name, p.name ) );
          // TDFile.renameFile( TDPath.getSurveyPlotC3dFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotC3dFile( name, p.name ) );
        }
      }
      /* FIXME_SKETCH_3D *
      { // rename sketch files: th3
        List< Sketch3dInfo > sketches = mData.selectAllSketches( sid );
        for ( Sketch3dInfo s : sketches ) {
          TDFile.renameFile( TDPath.getSurveySketchOutFile( TDInstance.survey, s.name ), TDPath.getSurveySketchOutFile( name, s.name ) );
        }
      }
       * FIXME_SKETCH_3D */
      // rename exported files: csv csx dat dxf kml plt srv svx th top tro 
        // TDFile.renameFile( TDPath.getSurveyThFile( TDInstance.survey ), TDPath.getSurveyThFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyCsvFile( TDInstance.survey ), TDPath.getSurveyCsvFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyCsxFile( TDInstance.survey ), TDPath.getSurveyCsxFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyCavFile( TDInstance.survey ), TDPath.getSurveyCavFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyCaveFile( TDInstance.survey ), TDPath.getSurveyCaveFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyDatFile( TDInstance.survey ), TDPath.getSurveyDatFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyDxfFile( TDInstance.survey ), TDPath.getSurveyDxfFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyGrtFile( TDInstance.survey ), TDPath.getSurveyGrtFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyGtxFile( TDInstance.survey ), TDPath.getSurveyGtxFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyKmlFile( TDInstance.survey ), TDPath.getSurveyKmlFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyJsonFile( TDInstance.survey ), TDPath.getSurveyJsonFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyPltFile( TDInstance.survey ), TDPath.getSurveyPltFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyShzFile( TDInstance.survey ), TDPath.getSurveyShzFile( name ) );
        // TDFile.renameFile( TDPath.getSurveySrvFile( TDInstance.survey ), TDPath.getSurveySrvFile( name ) );
        // TDFile.renameFile( TDPath.getSurveySurFile( TDInstance.survey ), TDPath.getSurveySurFile( name ) );
        // TDFile.renameFile( TDPath.getSurveySvxFile( TDInstance.survey ), TDPath.getSurveySvxFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyTopFile( TDInstance.survey ), TDPath.getSurveyTopFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyTrbFile( TDInstance.survey ), TDPath.getSurveyTrbFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyTroFile( TDInstance.survey ), TDPath.getSurveyTroFile( name ) );

      // rename note file: note
      TDFile.renameFile( TDPath.getSurveyNoteFile( TDInstance.survey ), TDPath.getSurveyNoteFile( name ) );
      // rename photo folder: photo
      TDFile.renameFile( TDPath.getSurveyPhotoDir( TDInstance.survey ), TDPath.getSurveyPhotoDir( name ) );
      // rename audio folder: audio
      TDFile.renameFile( TDPath.getSurveyAudioDir( TDInstance.survey ), TDPath.getSurveyAudioDir( name ) );
      // rename survey folder
      TDFile.renameFile( TDPath.getSurveyDir( TDInstance.survey ), TDPath.getSurveyDir( name ) );

      TDInstance.survey = name;
      return true;
    }
    return false;
  }
    
  /** update windows title and display
   */
  private static void updateWindows()
  {
    if ( mShotWindow != null) {
      mShotWindow.setTheTitle();
      mShotWindow.updateDisplay();
    }
    if ( mSurveyWindow != null ) {
      mSurveyWindow.setTheTitle();
      mSurveyWindow.updateDisplay();
    }
  }

  /**
   * @param name      survey name
   * @param datamode  survey datamode
   * @param update    whether to call a display update
   * @return survey ID
   */
  public long setSurveyFromName( String name, int datamode, boolean update )
  { 
    TDInstance.sid      = -1;       // no survey by default
    TDInstance.survey   = null;
    TDInstance.datamode = 0;
    // TDINstance.extend   = SurveyInfo.SURVEY_EXTEND_NORMAL;
    StationName.clearCurrentStation();
    ManualCalibration.reset();

    if ( name != null && mData != null ) {
      // TDLog.v( "set SurveyFromName <" + name + ">");

      TDInstance.sid = mData.setSurvey( name, datamode );

      // mFixed.clear();
      TDInstance.survey = null;
      if ( TDInstance.sid > 0 ) {
        TDPath.setSurveyPaths( name );
        DistoXStationName.setInitialStation( mData.getSurveyInitStation( TDInstance.sid ) );
        TDInstance.survey = name;
	TDInstance.datamode = mData.getSurveyDataMode( TDInstance.sid );
	// TDLog.v( "set survey from name: <" + name + "> datamode " + datamode + " " + TDInstance.datamode );
        TDInstance.secondLastShotId = lastShotId();
        // restoreFixed();
	if ( update ) updateWindows();
        TDInstance.xsections = ( SurveyInfo.XSECTION_SHARED == mData.getSurveyXSectionsMode( TDInstance.sid ) );

        // TDInstance.extend = 
        int extend = mData.getSurveyExtend( TDInstance.sid );
        // TDLog.v( "set SurveyFromName extend: " + extend );
        if ( SurveyInfo.isExtendLeft( extend ) ) { 
          TDAzimuth.mFixedExtend = -1L;
        } else if ( SurveyInfo.isExtendRight( extend ) ) { 
          TDAzimuth.mFixedExtend = 1L;
        } else {
          TDAzimuth.mFixedExtend = 0;
          TDAzimuth.mRefAzimuth  = extend;
        }
      }
      return TDInstance.sid;
    }
    return 0;
  }

  /** move survey data to a new survey
   * @param old_sid    old survey ID
   * @param old_id     old ID of start data to move to new survey
   * @param new_survey new survey name
   * @return true if success
   */
  boolean moveSurveyData( long old_sid, long old_id, String new_survey )
  {
    if ( mData == null ) return false;
    long new_sid = mData.getSurveyId( new_survey );
    // TDLog.v( "SID " + old_sid + " " + new_sid + " ID " + old_id );
    if ( new_sid <= 0 || new_sid == old_sid ) return false;
    return mData.moveShotsBetweenSurveys( old_sid, old_id, new_sid );
  }

  /** check if there exists a survey
   * @param name   survey name
   * @return true if the survey exists
   */
  boolean hasSurveyName( String name )
  {
    return ( mData != null ) && mData.hasSurveyName( name );
  }

  /** check if there exists a survey plot (for the current survey)
   * @param name   plot name
   * @return true if the plot exists
   */
  boolean hasSurveyPlotName( String name )
  {
    return ( mData != null ) && mData.hasSurveyPlotName( TDInstance.sid, name );
  }


  /** check if there exists a calibration
   * @param name   calibration name
   * @return true if the calibration exists
   */
  boolean hasCalibName( String name )
  {
    return ( mDData != null ) && mDData.hasCalibName( name );
  }

  void /*long*/ setCalibFromName( String calib ) // RETURN value not used
  {
    TDInstance.cid = -1;
    TDInstance.calib = null;
    if ( calib != null && mDData != null ) {
      TDInstance.cid = mDData.setCalib( calib );
      TDInstance.calib = (TDInstance.cid > 0)? calib : null;
      // return TDInstance.cid;
    }
    // return 0;
  }

  // -----------------------------------------------------------------
  // PREFERENCES

  // private void setDefaultSocketType( )
  // {
  //   // String defaultSockType = ( android.os.Build.MANUFACTURER.equals("samsung") ) ? "1" : "0";
  //   // TDPrefHelper.update( "DISTOX_SOCKET_TYPE", defaultSockType ); 
  //   TDPrefHelper.update( "DISTOX_SOCKET_TYPE", 1 ); // UNSECURE
  // }

  /** set the CWD preference
   * @param cwd   CWD (Current Work Directory)
   */
  static void setCWDPreference( String cwd /*, String cbd */ )
  { 
    if ( TDInstance.cwd.equals( cwd ) /* && TDInstance.cbd.equals( cbd ) */ ) return;
    // TDLog.v( "set CWD preference " + cwd );
    TDPrefHelper.update( "DISTOX_CWD", cwd /* , "DISTOX_CBD", cbd */ ); 
    setCWD( cwd /*, cbd */ ); 
  }

  /** set the PT map preference
   * @param cmap  PT map - PocketTopo color map
   */
  static void setPtCmapPreference( String cmap )
  {
    TDPrefHelper.update( "DISTOX_PT_CMAP", cmap ); 
    PtCmapActivity.setMap( cmap );
  }

  // unused
  // void setAccuracyPreference( float acceleration, float magnetic, float dip )
  // {
  //   TDPrefHelper.update( "DISTOX_ACCEL_THR", Float.toString( acceleration ), "DISTOX_MAG_THR", Float.toString( magnetic ), "DISTOX_DIP_THR", Float.toString( dip ) ); 
  // }

  /** set the text size
   * @param ts    text size
   */
  static void setTextSize( int ts )
  {
    TDSetting.setTextSize( ts );
    if ( TDSetting.setLabelSize( ts*3, false ) || TDSetting.setStationSize( ts*2, false ) ) { // false: do not update brush
      BrushManager.setTextSizes( );
    }
    TDPrefHelper.update( "DISTOX_TEXT_SIZE", Integer.toString(ts), "DISTOX_LABEL_SIZE", Float.toString(ts*3), "DISTOX_STATION_SIZE", Float.toString(ts*2) );
  }

  /** set the buttons size
   * @param bs    buttons size
   */
  static void setButtonSize( int bs )
  {
    // TDLog.v("set button size " + bs );
    TDSetting.setSizeButtons( bs );
    TDPrefHelper.update( "DISTOX_SIZE_BUTTONS", Integer.toString(bs) );
  }

  /** set the drawing icons units
   * @param u    drawing icons units
   */
  static void setDrawingUnitIcons( float u )
  {
    TDSetting.setDrawingUnitIcons( u );
    TDPrefHelper.update( "DISTOX_DRAWING_UNIT", Float.toString(u) );
  }

  /** set the drawing lines units
   * @param u    drawing lines units
   */
  static void setDrawingUnitLines( float u )
  {
    TDSetting.setDrawingUnitLines( u );
    TDPrefHelper.update( "DISTOX_LINE_UNITS", Float.toString(u) );
  }

  // used for "DISTOX_WELCOME_SCREEN" and "DISTOX_TD_SYMBOL"
  static void setBooleanPreference( String preference, boolean val ) { TDPrefHelper.update( preference, val ); }

  // FIXME_DEVICE_STATIC
  /** set the prinmary device
   * @param address   prinmary device address
   * @param model     device model
   * @param name      device name
   * @param bt_device bluetooth device of the device
   */
  void setDevicePrimary( String address, String model, String name, BluetoothDevice bt_device )
  { 
    deleteComm();
    if ( address == null ) { // null, ..., ...
      TDInstance.setDeviceA( null );
      address = TDString.EMPTY;
    } else {
      if ( model != null ) { // addr, model, ...
        // TDInstance.setDeviceA( new Device( address, model, 0, 0, name, null ) );
        // if ( Device.isBle( TDInstance.deviceType() ) ) TDInstance.initBleDevice();
        Device device = mDData.getDevice( address );
        if ( device == null ) device = new Device( address, model, 0, 0, name, null );
        TDInstance.setDeviceA( device );
        if ( device.isBLE() ) TDInstance.initBleDevice();
      } else if ( bt_device != null ) { // addr, null, dev
        model = Device.typeToString( TDInstance.deviceType() );
        // address, model, head, tail, name, nickname
        TDInstance.setDeviceA( new Device( address, model, 0, 0, name, null ) );
        TDInstance.setBleDevice( bt_device );
        if ( TDInstance.isDeviceSap() ) {
          mComm = new SapComm( this, address, bt_device ); 
        } else if ( TDInstance.isDeviceBric() ) {
          mComm = new BricComm( this, this, address, bt_device );
        }
      } else { // addr, null, null
        // boolean create = Device.isBle( TDInstance.deviceType() );
        TDInstance.setDeviceA( mDData.getDevice( address ) );
        // TDInstance.setBleDevice( null );
        // if ( create ) createComm();
        if ( Device.isBle( TDInstance.deviceType() ) ) TDInstance.initBleDevice();
      }
      createComm();
    }
    TDPrefHelper.update( TDSetting.keyDeviceName(), address ); 
    if ( mMainActivity != null ) mMainActivity.setButtonDevice();
  }

  // TODO BLE for the second DistoX
  /** set the address of the alternate device
   * @param address   alternate device address
   */
  void setDeviceB( String address )
  {
    if ( address == null ) {
      // if ( mVirtualDistoX != null ) mVirtualDistoX.stopServer( this ); // FIXME VirtualDistoX
      TDInstance.setDeviceB( null );
      // address = TDString.EMPTY;
    } else {
      TDInstance.setDeviceB( mDData.getDevice( address ) );
    }
  }

  /** switch to the alternate device
   * @return true if success
   */
  boolean switchSecondDevice()
  {
    return TDInstance.switchDevice();
  }

  // -------------------------------------------------------------
  // DATA BATCH DOWNLOAD

  /** 
   * @param lister    data lister
   * @param data_type packet datatype
   * @return number of packet received
   *
   * NOTE called only by DataDownloadTask
   */
  int downloadDataBatch( Handler /* ILister */ lister, int data_type ) // FIXME_LISTER
  {
    // TDLog.v( "App: download data batch");
    TDInstance.secondLastShotId = lastShotId();
    int ret = 0;
    if ( mComm == null || TDInstance.getDeviceA() == null ) {
      TDLog.Error( "Comm or Device null ");
    } else {
      // TDLog.Log( TDLog.LOG_DATA, "Download Data Batch() device " + TDInstance.deviceAddress() + " comm " + mComm.toString() );
      // TDLog.v( "App: Download Data Batch() device " + TDInstance.deviceAddress() + " " + TDInstance.getDeviceA().getAddress() + " comm " + mComm.toString() );
      ret = mComm.downloadData( TDInstance.getDeviceA().getAddress(), lister, data_type );
      // FIXME BATCH
      // if ( ret > 0 && TDSetting.mSurveyStations > 0 ) {
      //   // FIXME TODO select only shots after the last leg shots
      //   List< DBlock > list = mData.selectAllShots( TDInstance.sid, TDStataus.NORMAL );
      //   assign Stations( list );
      // }
    }
    return ret;
  }

  // =======================================================
  // StationName mStationName;

  // void resetCurrentStationName( String name ) { StationName.resetCurrentStationName( name ); }
  boolean setCurrentStationName( String name ) { return StationName.setCurrentStationName( name ); }
  String getCurrentStationName() { return StationName.getCurrentStationName(); }
  boolean isCurrentStationName( String name ) { return StationName.isCurrentStationName( name ); }
  // void clearCurrentStation() { StationName.clearCurrentStation(); }
  String getCurrentOrLastStation( ) { return StationName.getCurrentOrLastStation( mData, TDInstance.sid); }
  String getFirstStation( ) { return StationName.getFirstStation( mData, TDInstance.sid); }
  private void resetCurrentOrLastStation( ) { StationName.resetCurrentOrLastStation( mData, TDInstance.sid); }

  String getFirstPlotOrigin() { return ( TDInstance.sid < 0 )? null : mData.getFirstPlotOrigin( TDInstance.sid ); }


  // static long trobotmillis = 0L; // TROBOT_MILLIS

  /** called also by ShotWindow::updataBlockList
   * this re-assign stations to shots with station(s) already set
   * the list of stations is ordered by compare
   *
   * @param blk0 block after whcih to assign stations
   * @param list list of shot to assign
   * @return ???
   */
  boolean assignStationsAfter( DBlock blk0, List< DBlock > list )
  { 
    Set<String> sts = mData.selectAllStationsBefore( blk0.mId, TDInstance.sid /*, TDStatus.NORMAL */ );
    // TDLog.v("DATA " + "assign stations after " + blk0.Name() + " size " + list.size() + " stations " + sts.size() );
    // if ( TDSetting.mSurveyStations < 0 ) return;
    StationName.clearCurrentStation();
    if ( StationPolicy.doTopoRobot() ) {
      // long millis = SystemClock.uptimeMillis(); // TROBOT_MILLIS
      // if ( millis > trobotmillis + 10000 ) {
      //   TDToast.make( R.string.toporobot_warning );
      //   trobotmillis = millis;
      // }
      return new StationNameTRobot(this, mData, TDInstance.sid ).assignStationsAfter( blk0, list, sts );
    } else  if ( StationPolicy.doBacksight() ) {
      return new StationNameBacksight(this, mData, TDInstance.sid ).assignStationsAfter( blk0, list, sts );
    } else if ( StationPolicy.doTripod() ) {
      return new StationNameTripod( this, mData, TDInstance.sid ).assignStationsAfter( blk0, list, sts );
    } 
    return new StationNameDefault( this, mData, TDInstance.sid ).assignStationsAfter( blk0, list, sts );
  }

  /** called also by ShotWindow::updataBlockList
   * @param list blocks whose stations need to be set in the DB
   * @return true if a leg was assigned
   */
  boolean assignStationsAll(  List< DBlock > list )
  { 
    Set<String> sts = mData.selectAllStations( TDInstance.sid );
    int sz = list.size();
    if ( sz == 0 ) return false;
    // TDLog.Log( TDLog.LOG_DATA, "assign stations all: size " + sz + " blk[0] id " + list.get(0).mId );
    // TDLog.v("DATA " + "assign stations all: size " + sz + " blk[0] id " + list.get(0).mId );

    // if ( TDSetting.mSurveyStations < 0 ) return;
    if ( StationPolicy.doTopoRobot() ) {
      // long millis = SystemClock.uptimeMillis(); // TROBOT_MILLIS
      // if ( millis > trobotmillis + 10000 ) {
      //   TDToast.make( R.string.toporobot_warning );
      //   trobotmillis = millis;
      // }
      return new StationNameTRobot(this, mData, TDInstance.sid ).assignStations( list, sts );
    } else  if ( StationPolicy.doBacksight() ) {
      return new StationNameBacksight(this, mData, TDInstance.sid ).assignStations( list, sts );
    } else if ( StationPolicy.doTripod() ) {
      return new StationNameTripod( this, mData, TDInstance.sid ).assignStations( list, sts );
    } 
    return new StationNameDefault( this, mData, TDInstance.sid ).assignStations( list, sts );
  }

  // ================================================================
  // EXPORTS

  /** 
   * @param context   context
   * @param uri       expot URI or null (to export to private folder)
   * @param origin    sketch origin
   * @param psd1      plot data
   * @param psd2      profile data
   * @param toast     whther to toast to result
   */
  static void exportSurveyAsCsxAsync( Context context, Uri uri, String origin, PlotSaveData psd1, PlotSaveData psd2, boolean toast )
  {
    SurveyInfo info = getSurveyInfo();
    if ( info == null ) {
      TDLog.Error("Error: null survey info");
      // TDLog.v( "null survey info");
      return;
    }
    String fullname = ( psd1 == null )? TDInstance.survey
                                      : TDInstance.survey + "-" + psd1.name;
    // String filename = TDPath.getSurveyCsxFile( fullname );
    // TDLog.Log( TDLog.LOG_IO, "exporting as CSX " + fullname + " " + filename );
    TDLog.Log( TDLog.LOG_IO, "exporting as CSX " + fullname );
    (new SaveFullFileTask( context, uri, TDInstance.sid, mData, info, psd1, psd2, origin, /* filename, */ fullname, 
       /* TDPath.getCsxFile(""), */ toast )).execute();
  }

  // FIXME_SYNC might be a problem with big surveys
  // this is called sync to pass the therion file to the 3D viewwer
  // static boolean exportSurveyAsThSync( )
  // {
  //   SurveyInfo info = getSurveyInfo();
  //   if ( info == null ) return false;
  //   // if ( async ) {
  //   //   String saving = context.getResources().getString(R.string.saving_);
  //   //   (new SaveDataFileTask( saving, TDInstance.sid, info, mData, TDInstance.survey, null, TDConst.SURVEY_FORMAT_TH, toast )).execute();
  //   //   return true;
  //   // }
  //   return ( TDExporter.exportSurveyAsTh( TDInstance.sid, mData, info, TDPath.getSurveyThFile( TDInstance.survey ) ) != null );
  // }

  // FIXME_SYNC ok because calib files are small
  String exportCalibAsCsv( )
  {
    if ( TDInstance.cid < 0 ) return null;
    CalibInfo ci = mDData.selectCalibInfo( TDInstance.cid );
    if ( ci == null ) return null;
    // TDPath.checkCCsvDir();
    // String filename = TDPath.getCCsvFile( ci.name );
    // return TDExporter.exportCalibAsCsv( TDInstance.cid, mDData, ci, filename );
    // File file = TDPath.getCcsvFile( ci.name + ".csv" );
    return CalibExport.exportCalibAsCsv( TDInstance.cid, mDData, ci, ci.name );
  }

  // ----------------------------------------------
  // FIRMWARE 

  /** install the firmware files
   * @param overwrite whether to overwrite existing files
   */
  static private void installFirmware( boolean overwrite )
  {
    TDLog.v("APP FW install firmware. overwrite: " + overwrite );
    InputStream is = TDInstance.getResources().openRawResource( R.raw.firmware );
    firmwareUncompress( is, overwrite );
    try { is.close(); } catch ( IOException e ) { }
  }
 
  // -------------------------------------------------------------
  // SYMBOLS

  /** install default (speleo) symbols
   * @param overwrite whether to overwrite existing files
   */
  static void installSymbols( boolean overwrite )
  {
    deleteObsoleteSymbols();
    // TDLog.v("PATH " + "install symbol version " + TDVersion.SYMBOL_VERSION );
    installSymbols( R.raw.symbols_speleo, overwrite );
    mDData.setValue( "symbol_version", TDVersion.SYMBOL_VERSION );
  }

  /** install a symbol set
   * @param res       symbol set resource 
   * @param overwrite whether to overwrite existing files
   */
  static void installSymbols( int res, boolean overwrite )
  {
    InputStream is = TDInstance.getResources().openRawResource( res );
    symbolsUncompress( is, overwrite );
  }

  /** delete the files of obsolete symbols 
   */
  static private void deleteObsoleteSymbols()
  {
    String[] lines = { 
      SymbolLibrary.BLOCKS,
      SymbolLibrary.DEBRIS,
      SymbolLibrary.CLAY,
      SymbolLibrary.CONTOUR,
      SymbolLibrary.WALL_PRESUMED,
      SymbolLibrary.SAND,
      SymbolLibrary.ICE,
      SymbolLibrary.SECTION,
      "wall:sand"
    };
    for ( String line : lines ) {
      TDPath.deleteLineFile( line );
    }
    String[] points = {
      "breakdown-choke",
      "low-end",
      "paleo-flow",
      SymbolLibrary.SECTION
    };
    for ( String point : points ) {
      TDPath.deletePointFile( point );
    }
  }

  void reloadSymbols( boolean clear, 
                      boolean speleo, boolean extra, boolean mine, boolean geo, boolean archeo, boolean anthro, boolean paleo,
                      boolean bio,    boolean karst )
  {
    // TDLog.v( "Reload symbols " + speleo + " " + mine + " " + geo + " " + archeo + " " + paleo + " " + bio + " clear " + clear );
    // if ( extra ) speleo = true; // extra implies speleo

    if ( clear ) {
      if (speleo || extra || mine || geo || archeo || anthro || paleo || bio || karst ) { 
        TDPath.clearSymbols();
      }
    }
    if ( speleo ) installSymbols( R.raw.symbols_speleo, true );
    if ( extra  ) installSymbols( R.raw.symbols_extra,  true );
    if ( mine   ) installSymbols( R.raw.symbols_mine,   true );
    if ( geo    ) installSymbols( R.raw.symbols_geo,    true );
    if ( archeo ) installSymbols( R.raw.symbols_archeo, true );
    if ( anthro ) installSymbols( R.raw.symbols_anthro, true );
    if ( paleo  ) installSymbols( R.raw.symbols_paleo,  true );
    if ( bio    ) installSymbols( R.raw.symbols_bio,    true );
    if ( karst  ) installSymbols( R.raw.symbols_karst,  true );

    mDData.setValue( "symbol_version", TDVersion.SYMBOL_VERSION );
    BrushManager.loadAllLibraries( this, getResources() );
    DrawingSurface.clearManagersCache();
  }

  /** decompress the symbols files
   * @param fis       input zip-file stream
   * @param overwrite whether to overwrite existing files
   * 
   * @note the output symbols files are in the private "point", "line", and "area" folders, 
   *       their names are taken from the zip entries
   */
  static private void symbolsUncompress( InputStream fis, boolean overwrite )
  {
    TDLog.v("APP uncompressing symbols - overwrite " + overwrite );
    // TDPath.symbolsCheckDirs();
    try {
      // byte buffer[] = new byte[36768];
      byte[] buffer = new byte[4096];
      ZipEntry ze = null;
      ZipInputStream zin = new ZipInputStream( fis );
      while ( ( ze = zin.getNextEntry() ) != null ) {
        String filepath = ze.getName();
        if ( filepath.endsWith("README") ) continue;
        if ( ! ze.isDirectory() ) {
          int pos = 0;
          if ( filepath.startsWith( "symbol" ) ) {
            pos  = filepath.indexOf('/');
            filepath = filepath.substring( pos+1 );
          }
          pos  = filepath.indexOf('/');
          String type = filepath.substring( 0, pos );
          filepath = filepath.substring( pos+1 );
          
          // String pathname = TDPath.getSymbolFile( filepath );
          // File file = TDFile.getPrivateFile( type, filepath );
          // TDLog.v("APP uncompress symbol " + type + " " + filepath );
          if ( overwrite || ! TDFile.existPrivateFile( type, filepath ) ) {
            // APP_SAVE SYMBOLS LOAD_MISSING
            // if ( file.exists() ) {
            //   if ( ! file.renameTo( TDFile.getFile( TDPath.getSymbolSaveFile( filepath ) ) ) ) TDLog.Error("File rename error");
            // }

            // TDPath.checkPath( pathname );
            // FileOutputStream fout = TDFile.getFileOutputStream( pathname );
            FileOutputStream fout = TDFile.getPrivateFileOutputStream( type, filepath );
            int c;
            while ( ( c = zin.read( buffer ) ) != -1 ) {
              fout.write(buffer, 0, c); // offset 0 in buffer
            }
            fout.close();
          }
        }
        zin.closeEntry();
      }
      zin.close();
    } catch ( FileNotFoundException e ) {
    } catch ( IOException e ) {
    }
  }

  // -------------------------------------------------------------------

  /** decompress the firmwares files
   * @param fis       input zip-file stream
   * @param overwrite whether to overwrite existing files
   * 
   * @note the output files are in the private "bin" folder, and their names are taken from the zip entries
   */
  static private void firmwareUncompress( InputStream fis, boolean overwrite )
  {
    // TDLog.v("FW " + "firmware uncompress ...");
    // TDPath.checkBinDir( );
    try {
      // byte buffer[] = new byte[36768];
      byte[] buffer = new byte[4096];
      ZipEntry ze = null;
      ZipInputStream zin = new ZipInputStream( fis );
      while ( ( ze = zin.getNextEntry() ) != null ) {
        String filepath = ze.getName();
        // TDLog.v( "firmware uncompress path " + filepath );
        if ( ze.isDirectory() ) continue;
        if ( ! filepath.endsWith( TDPath.DIR_BIN ) ) continue;
        // String pathname =  TDPath.getBinFilename( filepath );
        File file = TDPath.getBinFile( filepath ); // PRIVATE FILE
        if ( overwrite || ! file.exists() ) {
          // TDPath.checkPath( pathname );
          // FileOutputStream fout = TDFile.getFileOutputStream( pathname );
          FileOutputStream fout = new FileOutputStream( file );
          int c;
          while ( ( c = zin.read( buffer ) ) != -1 ) {
            fout.write(buffer, 0, c); // offset 0 in buffer
          }
          fout.close();
        }
        zin.closeEntry();
      }
      zin.close();
    } catch ( FileNotFoundException e ) {
    } catch ( IOException e ) {
    }
  }

  // ---------------------------------------------------------
  /** insert LRUD splays at a given station, before shot with id "at"
   * @param at       block id before which to insert the LRUD
   * @param splay_station station of the LRUD splay
   * @param bearing  block azimuth
   * @param clino    bock clino
   * @param left     LEFT length
   * @param right    RIGHT length
   * @param up       UP length
   * @param down     DOWN length
   * could return the long at
   */
  long insertLRUDatStation( long at, String splay_station, float bearing, float clino,
                            String left, String right, String up, String down )
  {
    // TDLog.v("LRUD " + "isert LRUD " + at + " station " + splay_station );
    return addManualSplays( at, splay_station, left, right, up, down, bearing, false, true ); // horizontal=false, true=ret +/-1;
  }

  /**
    * @param from     FROM station
    * @param to       TO station
    * @param distance user-input distance (current units)
    * @param bearing  from block
    * @param clino    from block
    * @param extend   ...
    * @return id of inserted leg
    * note before inserting the duplicate leg it set the CurrentStationName
    */
  long insertDuplicateLeg( String from, String to, float distance, float bearing, float clino, int extend )
  {
    // reset current station so that the next shot does not attach to the intermediate leg
    resetCurrentOrLastStation( );
    long millis = java.lang.System.currentTimeMillis()/1000;
    distance = distance / TDSetting.mUnitLength;
    // TDLog.v( "[2] dupl.-shot Data " + distance + " " + bearing + " " + clino );
    long id = mData.insertManualShot( TDInstance.sid, -1L, millis, 0, distance, bearing, clino, 0.0f, extend, 0.0, LegType.NORMAL, 1 );
    if ( mData.checkSiblings( id, TDInstance.sid, from, to, distance, bearing, clino ) ) {
      // TDLog.v("APP insert duplicate leg detect bad sibling");
      TDToast.makeWarn( R.string.bad_sibling );
    }
    mData.updateShotName( id, TDInstance.sid, from, to );
    mData.updateShotFlag( id, TDInstance.sid, DBlock.FLAG_DUPLICATE );
    return id;
  }

  /**
   * @param at       station where to add (?)
   * @param splay_station station of the splays (?)
   * @param left     left splay 
   * @param right    right splay 
   * @param up       up splay 
   * @param down     down splay 
   * @param bearing       leg azimuth
   * @param horizontal    whether WENS or LRUD
   * @param ret_success   ...
   */
  private long addManualSplays( long at, String splay_station, String left, String right, String up, String down,
                                float bearing, boolean horizontal, boolean ret_success )
  {
    long id;
    long millis = java.lang.System.currentTimeMillis()/1000;
    long extend = 0L;
    float calib = ManualCalibration.mLRUD ? ManualCalibration.mLength : 0;
    float l = -1.0f;
    float r = -1.0f;
    float u = -1.0f;
    float d = -1.0f;
    boolean ok = false;
    if ( left != null && left.length() > 0 ) {
      try {
        l = Float.parseFloat( left ) / TDSetting.mUnitLength - calib;
      } catch ( NumberFormatException e ) {
        TDLog.Error( "manual-shot parse error: left " + ((left==null)?"null":left) );
      }
    }  
    if ( right != null && right.length() > 0 ) {
      try {
        r = Float.parseFloat( right ) / TDSetting.mUnitLength - calib;
      } catch ( NumberFormatException e ) {
        TDLog.Error( "manual-shot parse error: right " + ((right==null)?"null":right) );
      }
    }
    if ( up != null && up.length() > 0 ) {
      try {
        u = Float.parseFloat( up ) / TDSetting.mUnitLength - calib;
      } catch ( NumberFormatException e ) {
        TDLog.Error( "manual-shot parse error: up " + ((up==null)?"null":up) );
      }
    }
    if ( down != null && down.length() > 0 ) {
      try {
        d = Float.parseFloat( down ) / TDSetting.mUnitLength - calib;
      } catch ( NumberFormatException e ) {
        TDLog.Error( "manual-shot parse error: down " + ((down==null)?"null":down) );
      }
    }

    extend = ExtendType.EXTEND_IGNORE;
    if ( l >= 0.0f ) { // FIXME_X_SPLAY
      ok = true;
      if ( horizontal ) { // WENS
        // extend = TDAzimuth.computeSplayExtend( 270 );
        // extend = ( TDSetting.mLRExtend )? TDAzimuth.computeSplayExtend( 270 ) : ExtendType.EXTEND_UNSET;
        if ( at >= 0L ) {
          id = mData.insertManualShotAt( TDInstance.sid, at, millis, 0, l, 270.0f, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
          ++at;
        } else {
          id = mData.insertManualShot( TDInstance.sid, -1L, millis, 0, l, 270.0f, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
        }
      } else {
        float b = bearing - 90.0f;
        if ( b < 0.0f ) b += 360.0f;
        // extend = TDAzimuth.computeSplayExtend( b );
        // extend = ( TDSetting.mLRExtend )? TDAzimuth.computeSplayExtend( b ) : ExtendType.EXTEND_UNSET;
        // b = in360( b );
        if ( at >= 0L ) {
          id = mData.insertManualShotAt( TDInstance.sid, at, millis, 0, l, b, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
          ++at;
        } else {
          id = mData.insertManualShot( TDInstance.sid, -1L, millis, 0, l, b, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
        }
      }
      mData.updateShotName( id, TDInstance.sid, splay_station, TDString.EMPTY );
      // TDLog.v("LRUD " + "insert " + id + " left " + l );
    }
    if ( r >= 0.0f ) {
      ok = true;
      if ( horizontal ) { // WENS
        // extend = TDAzimuth.computeSplayExtend( 90 );
        // extend = ( TDSetting.mLRExtend )? TDAzimuth.computeSplayExtend( 90 ) : ExtendType.EXTEND_UNSET;
        if ( at >= 0L ) {
          id = mData.insertManualShotAt( TDInstance.sid, at, millis, 0, r, 90.0f, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
          ++at;
        } else {
          id = mData.insertManualShot( TDInstance.sid, -1L, millis, 0, r, 90.0f, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
        }
      } else {
        // float b = bearing + 90.0f; if ( b >= 360.0f ) b -= 360.0f;
        float b = TDMath.add90( bearing );
        // extend = TDAzimuth.computeSplayExtend( b );
        // extend = ( TDSetting.mLRExtend )? TDAzimuth.computeSplayExtend( b ) : ExtendType.EXTEND_UNSET;
        if ( at >= 0L ) {
          id = mData.insertManualShotAt( TDInstance.sid, at, millis, 0, r, b, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
          ++at;
        } else {
          id = mData.insertManualShot( TDInstance.sid, -1L, millis, 0, r, b, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
        }
      }
      mData.updateShotName( id, TDInstance.sid, splay_station, TDString.EMPTY );
      // TDLog.v("LRUD " + "insert " + id + " right " + r );
    }
    extend = ExtendType.EXTEND_VERT;
    if ( u >= 0.0f ) {  
      ok = true;
      if ( horizontal ) {
        if ( at >= 0L ) {
          id = mData.insertManualShotAt( TDInstance.sid, at, millis, 0, u, 0.0f, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
          ++at;
        } else {
          id = mData.insertManualShot( TDInstance.sid, -1L, millis, 0, u, 0.0f, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
        }
      } else {
        if ( at >= 0L ) {
          id = mData.insertManualShotAt( TDInstance.sid, at, millis, 0, u, 0.0f, 90.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
          ++at;
        } else {
          id = mData.insertManualShot( TDInstance.sid, -1L, millis, 0, u, 0.0f, 90.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
        }
      }
      mData.updateShotName( id, TDInstance.sid, splay_station, TDString.EMPTY );
      // TDLog.v("LRUD " + "insert " + id + " up " + u );
    }
    if ( d >= 0.0f ) {
      ok = true;
      if ( horizontal ) {
        if ( at >= 0L ) {
          id = mData.insertManualShotAt( TDInstance.sid, at, millis, 0, d, 180.0f, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
          ++at;
        } else {
          id = mData.insertManualShot( TDInstance.sid, -1L, millis, 0, d, 180.0f, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
        }
      } else {
        if ( at >= 0L ) {
          id = mData.insertManualShotAt( TDInstance.sid, at, millis, 0, d, 0.0f, -90.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
          ++at;
        } else {
          id = mData.insertManualShot( TDInstance.sid, -1L, millis, 0, d, 0.0f, -90.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
        }
      }
      mData.updateShotName( id, TDInstance.sid, splay_station, TDString.EMPTY );
      // TDLog.v("LRUD " + "insert " + id + " down " + d );
    }
    // TDLog.v("LRUD " + "add manual returns " + at );
    if ( ret_success ) return (ok ? 1L : -1L);
    return at;
  }

  /** insert manual-data shot
   * @param at   id of the shot before which to insert the new shot (and LRUD)
   * @param from     from station
   * @param to       to station
   * @param distance shot length
   * @param bearing  shot aimuth
   * @param clino    shot clino
   * @param extend0  extend
   * @param flag0    flags
   * @param left     left splay 
   * @param right    right splay 
   * @param up       up splay 
   * @param down     down splay 
   * @param splay_station station of splay shots
   * @return inserted block or null on failure
   * 
   * @note manual shots take into account the instruments calibrations, LRUD can also be affected
   */
  DBlock insertManualShot( long at, String from, String to,
                           float distance, float bearing, float clino, long extend0, long flag0,
                           String left, String right, String up, String down,
                           String splay_station )
  {
    TDInstance.secondLastShotId = lastShotId();
    DBlock ret = null;
    long id;
    long millis = java.lang.System.currentTimeMillis()/1000;

    distance = distance / TDSetting.mUnitLength - ManualCalibration.mLength;
    clino    = clino    / TDSetting.mUnitAngle  - ManualCalibration.mClino;
    float b  = bearing  / TDSetting.mUnitAngle;


    if ( ( distance < 0.0f ) ||
         ( clino < -90.0f || clino > 90.0f ) ||
         ( b < 0.0f || b >= 360.0f ) ) {
      TDToast.makeBad( R.string.illegal_data_value );
      return null;
    }
    bearing = TDMath.in360( bearing / TDSetting.mUnitAngle - ManualCalibration.mAzimuth );

    if ( from != null && to != null && from.length() > 0 ) {
      // if ( mData.makesCycle( -1L, TDInstance.sid, from, to ) ) {
      //   TDToast.make( R.string.makes_cycle );
      // } else
      {
        // TDLog.Log( TDLog.LOG_SHOT, "manual-shot Data " + distance + " " + bearing + " " + clino );
        boolean horizontal = ( Math.abs( clino ) > TDSetting.mVThreshold );
        // TDLog.Log( TDLog.LOG_SHOT, "manual-shot SID " + TDInstance.sid + " LRUD " + left + " " + right + " " + up + " " + down);
        if ( StationPolicy.mShotAfterSplays ) {
          // TDLog.v( "[1] manual-shot Data " + distance + " " + bearing + " " + clino );
          at = addManualSplays( at, splay_station, left, right, up, down, bearing, horizontal, false );

          if ( at >= 0L ) {
            id = mData.insertManualShotAt( TDInstance.sid, at, millis, 0, distance, bearing, clino, 0.0f, extend0, 0.0, LegType.NORMAL, 1 );
          } else {
            id = mData.insertManualShot( TDInstance.sid, -1L, millis, 0, distance, bearing, clino, 0.0f, extend0, 0.0, LegType.NORMAL, 1 );
          }
          // String name = from + "-" + to;
          mData.updateShotName( id, TDInstance.sid, from, to );
          if ( mData.checkSiblings( id, TDInstance.sid, from, to, distance, bearing, clino ) ) {
            // TDLog.v("APP insert manual shot detect bad sibling");
            TDToast.makeWarn( R.string.bad_sibling );
          }
          // mData.updateShotExtend( id, TDInstance.sid, extend0, stretch0 );
          // mData.updateShotExtend( id, TDInstance.sid, ExtendType.EXTEND_IGNORE, 1 ); // FIXME WHY ???
          // FIXME updateDisplay( );
        } else {
          // TDLog.v( "[2] manual-shot Data " + distance + " " + bearing + " " + clino );
          if ( at >= 0L ) {
            id = mData.insertManualShotAt( TDInstance.sid, at, millis, 0, distance, bearing, clino, 0.0f, extend0, 0.0, LegType.NORMAL, 1 );
            ++ at;
          } else {
            id = mData.insertManualShot( TDInstance.sid, -1L, millis, 0, distance, bearing, clino, 0.0f, extend0, 0.0, LegType.NORMAL, 1 );
          }
          // String name = from + "-" + to;
          mData.updateShotName( id, TDInstance.sid, from, to );
          // mData.updateShotExtend( id, TDInstance.sid, extend0, stretch0 );
          // mData.updateShotExtend( id, TDInstance.sid, ExtendType.EXTEND_IGNORE, 1 );  // FIXME WHY ???
          // FIXME updateDisplay( );

          addManualSplays( at, splay_station, left, right, up, down, bearing, horizontal, false );
        }
        ret = mData.selectShot( id, TDInstance.sid );
      }
    } else {
      TDToast.makeBad( R.string.missing_station );
    }
    return ret;
  }

  /** guess the algo from the info in the database of the current calibration 
   * @return algo code (@see CalibInfo)
   */
  int getCalibAlgoFromDB()
  {
    return mDData.selectCalibAlgo( TDInstance.cid );
  }

  /** update the algo of the current calibration in the database
   * @param algo   algo code (@see CalibInfo)
   */
  void updateCalibAlgo( int algo ) 
  {
    mDData.updateCalibAlgo( TDInstance.cid, algo );
  }
  
  /** guess the calibration algo from the device infos
   * @return algo code (@see CalibInfo)
   */
  int getCalibAlgoFromDevice()
  {
    if ( TDInstance.getDeviceA() == null ) return CalibInfo.ALGO_LINEAR;
    if ( TDInstance.isDeviceA3() ) return CalibInfo.ALGO_LINEAR; // A3
    if ( TDInstance.isDeviceX310() ) {
      if ( mComm == null ) return 1; // should not happen
      byte[] ret = mComm.readMemory( TDInstance.deviceAddress(), 0xe000 );
      if ( ret != null && ( ret[0] >= 2 && ret[1] >= 3 ) ) return CalibInfo.ALGO_NON_LINEAR;
    }
    return CalibInfo.ALGO_LINEAR; // default
  }  

  // --------------------------------------------------------

  /** send a command to the BRIC
   * @param cmd   command code (@see BricConst)
   */
  public void sendBricCommand( int cmd )
  { 
    // boolean ret = false;
    if ( mComm != null && mComm instanceof BricComm ) {
      // TDLog.v( "App: send bric command " + cmd );
      mComm.sendCommand( cmd );
      // TDToast( R.string.bric_command_fail ); // should never happen to fail
    // } else {
    //   TDLog.Error("Comm is null or not BRIC");
    }
    // return ret;
  }

  /** retrieve the BRIC info
   * @param info   info display dialog
   * @return true if successful
   */
  public boolean getBricInfo( BricInfoDialog info )
  {
    if ( mComm != null && mComm instanceof BricComm ) {
      BricComm comm = (BricComm)mComm;
      boolean disconnect = comm.isConnected();
      if ( ! disconnect ) {
        connectDevice( TDInstance.deviceAddress(), DataType.DATA_ALL );
        // TDLog.v("BRIC info: wait 4 secs");
        TDUtil.yieldDown(4000); // FIXME was 4000
      }
      if ( comm.isConnected() ) {
        comm.registerInfo( info );
        info.getInfo( comm );
        if ( disconnect ) disconnectComm();
        return true;
      } else {
        TDLog.Error("BRIC info: failed to connect");
      }
    }
    return false;
  }

  /** set BRIC memory
   * @param bytes ...
   * @return true if successful
   */
  public boolean setBricMemory( byte[] bytes )
  {
    // TDLog.v( "set BRIC memory - ... " + ( (bytes == null)? "clear" : bytes[4] + ":" + bytes[5] + ":" + bytes[6] ) );
    boolean ret = false;
    if ( mComm != null && mComm instanceof BricComm ) {
      BricComm comm = (BricComm)mComm;
      boolean disconnect = comm.isConnected();
      if ( ! disconnect ) {
        connectDevice( TDInstance.deviceAddress(), DataType.DATA_ALL );
        // TDLog.v("BRIC memory: wait 4 secs");
        TDUtil.yieldDown(4000);
      }
      if ( comm.isConnected() ) {
        ret = comm.setMemory( bytes );
        if ( disconnect ) disconnectComm();
        return true;
      } else {
        TDLog.Error( "BRIC memory: failed to connect");
      }
    }
    return ret;
  }


  /** set the X310 laser
   * @param what      what to do:  0: off, 1: on, 2: measure
   * @param nr        number od data to download
   # @param lister    optional lister
   * @param data_type type of expected data
   */
  public void setX310Laser( int what, int nr, Handler /* ILister */ lister, int data_type ) // FIXME_LISTER
  {
    if ( mComm == null || TDInstance.getDeviceA() == null ) return;
    if ( mComm instanceof DistoX310Comm ) {
      DistoX310Comm comm = (DistoX310Comm)mComm;
      if ( comm != null ) comm.setX310Laser( TDInstance.deviceAddress(), what, nr, lister, data_type );
    } else {
      TDLog.Error("set X310 laser: not X310 comm");
    }
  }

  // int readFirmwareHardware()
  // {
  //   return mComm.readFirmwareHardware( TDInstance.getDeviceA().getAddress() );
  // }

  /** read the firmware signature - only X310
   * @param hw expected device hardware
   * @return firmware signtaure (or null if failure)
   */
  public byte[] readFirmwareSignature( int hw )
  {
    TDLog.v("APP FW read signature - HW " + hw );
    // FIXME ASYNC_FIRMWARE_TASK
    // if ( mComm == null || TDInstance.getDeviceA() == null ) return;
    // if ( ! (mComm instanceof DistoX310Comm) ) return;
    // (new FirmwareTask( (DistoX310Comm)mComm, FirmwareTask.FIRMWARE_SIGN, filename )).execute( );

    if ( mComm == null || TDInstance.getDeviceA() == null ) return null;
    if ( ! (mComm instanceof DistoX310Comm) ) return null;
    return ((DistoX310Comm)mComm).readFirmwareSignature( TDInstance.deviceAddress(), hw );
  }

  /** read the firmware and save it to a file - only X310
   * @param name   filename including ".bin" extension
   * @return ...
   */
  public int dumpFirmware( String name )
  {
    TDLog.v("APP FW dump " + name );
    // FIXME ASYNC_FIRMWARE_TASK
    // if ( mComm == null || TDInstance.getDeviceA() == null ) return;
    // if ( ! (mComm instanceof DistoX310Comm) ) return;
    // (new FirmwareTask( (DistoX310Comm)mComm, FirmwareTask.FIRMWARE_READ, filename )).execute( );

    if ( mComm == null || TDInstance.getDeviceA() == null ) return -1;
    if ( ! (mComm instanceof DistoX310Comm) ) return -1;
    // return ((DistoX310Comm)mComm).dumpFirmware( TDInstance.deviceAddress(), TDPath.getBinFilename(name) );
    return ((DistoX310Comm)mComm).dumpFirmware( TDInstance.deviceAddress(), TDPath.getBinFile( name ) );
  }

  /** read a firmware reading it from a file - only X310
   * @param name   filename including ".bin" extension
   * @return ...
   */
  public int uploadFirmware( String name )
  {
    TDLog.v("APP FW upload " + name );
    // FIXME ASYNC_FIRMWARE_TASK
    // if ( mComm == null || TDInstance.getDeviceA() == null ) return;
    // if ( ! (mComm instanceof DistoX310Comm) ) return;
    // String pathname = TDPath.getBinFilename( name );
    // TDLog.LogFile( "Firmware upload address " + TDInstance.deviceAddress() );
    // TDLog.LogFile( "Firmware upload file " + pathname );
    // (new FirmwareTask( (DistoX310Comm)mComm, FirmwareTask.FIRMWARE_WRITE, name )).execute( ); 

    if ( mComm == null || TDInstance.getDeviceA() == null ) return -1;
    if ( ! (mComm instanceof DistoX310Comm) ) return -1;
    File file = TDPath.getBinFile( name ); // PRIVATE FILE
    TDLog.LogFile( "Firmware upload address " + TDInstance.deviceAddress() + " file " + file.getPath() );
    // TDLog.LogFile( "Firmware upload file " + file.getPath() );
    TDLog.v("APP FW upload file " + file.getPath() );
    // return ((DistoX310Comm)mComm).uploadFirmware( TDInstance.deviceAddress(), pathname );
    return ((DistoX310Comm)mComm).uploadFirmware( TDInstance.deviceAddress(), file );
  }

  // ----------------------------------------------------------------------

  /** insert a new 2D plot
   * @param sid      survey ID
   * @param name     plot name
   * @param start    plot base station (origin)
   * @param extended whether the plot is extended profile
   * @param project  projection angle (for profile)
   * @return plot ID
   */
  public long insert2dPlot( long sid , String name, String start, boolean extended, int project )
  {
    // PlotInfo.ORIENTATION_PORTRAIT = 0
    // TDLog.Log( TDLog.LOG_PLOT, "new plot " + name + " start " + start );
    long pid_p = mData.insertPlot( sid, -1L, name+"p",
                 PlotType.PLOT_PLAN, 0L, start, TDString.EMPTY, 0, 0, mScaleFactor, 0, 0, TDString.EMPTY, TDString.EMPTY, 0 );
    if ( extended ) {
      long pid_s = mData.insertPlot( sid, -1L, name+"s",
                   PlotType.PLOT_EXTENDED, 0L, start, TDString.EMPTY, 0, 0, mScaleFactor, 0, 0, TDString.EMPTY, TDString.EMPTY, 0 );
    } else {
      long pid_s = mData.insertPlot( sid, -1L, name+"s",
                   PlotType.PLOT_PROJECTED, 0L, start, TDString.EMPTY, 0, 0, mScaleFactor, project, 0, TDString.EMPTY, TDString.EMPTY, 0 );
    }
    return pid_p;
  }
  
  /** insert a new XSection
   * @param sid      survey ID
   * @param name     section name
   * @param type     X-section type
   * @param from     viewing station
   * @param to       viewed station
   * @param azimuth  projected profile azimuth / section plane direction 
   * @param clino    projected profile clino / section plane direction 
   * @param parent   name of parent plot
   * @param nickname xsection comment
   * @return XSection plot ID
   * 
   * @note the database field "hide" is overloaded for x_sections with the parent plot name
   */
  public long insert2dSection( long sid, String name, long type, String from, String to, float azimuth, float clino, String parent, String nickname )
  {
    // FIXME COSURVEY 2d sections are not forwarded
    // pid = -1L   assign new PID
    // status = 0  normal
    // X,Y offset  0,0
    // zoom        mScaleFactor
    // orientation 0
    // TDLog.v("APP insert 2D section from <" + from + "> to <" + to + ">" );
    String hide = ( parent == null )? TDString.EMPTY : parent;
    String nick = ( nickname == null )? TDString.EMPTY : nickname;
    // FIXME the reason for this offset is not clear - especially why 3.5 ?
    float xoff = mDisplayWidth / (2*mScaleFactor) - DrawingUtil.CENTER_X;
    float yoff = mDisplayHeight / (2*mScaleFactor) - DrawingUtil.CENTER_Y;
    // TDLog.v("size " + mDisplayWidth + " " + mDisplayHeight + " offset " + xoff + " " + yoff + " density " + getDisplayDensity() + " " + getDisplayDensityDpi() );
    return mData.insertPlot( sid, -1L, name, type, 0L, from, to, xoff, yoff, mScaleFactor, azimuth, clino, hide, nick, 0 );
  }

  // @param ctx       context
  // @prarm filename  photo filename
  // static void viewPhoto( Context ctx, String filename )
  // {
  //   // TDLog.v( "photo <" + filename + ">" );
  //   File file = TDFile.getFile( filename );
  //   if ( file.exists() ) {
  //     // FIXME create a dialog like QCam that displays the JPEG file
  //     //
  //     // Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("file://" + filename ) );
  //     
  //     if ( TDandroid.BELOW_API_24 ) {
  //       Intent intent = new Intent(Intent.ACTION_VIEW );
  //       intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
  //       intent.setDataAndType( Uri.fromFile( file ), "image/jpeg" ); // pre Nougat
  //       // } else {
  //       //   URI apkURI = FileProvider.getUriForFile( ctx, ctx.getApplicationContext().getPackageName() + ".provider", file );
  //       //   intent.setDataAndType( apkURI, "image/jpeg" );
  //       //   intent.addFlags( Intent.FLAG_GRANT_READ_URI_PERMISSION );
  //       try {
  //         ctx.startActivity( intent );
  //       } catch ( ActivityNotFoundException e ) {
  //         // gracefully fail without saying anything
  //       }
  //     } else {
  //       TDToast.makeBad( "Photo display not yet implemented" );
  //     }
  //   } else {
  //     TDToast.makeBad( "ERROR file not found: " + filename );
  //   }
  // }

  // ---------------------------------------------------------------------
  /* ---- IF_COSURVEY

  // DataListener (co-surveying)
  private DataListenerSet mDataListeners;

  // synchronized( mDataListener )
  void registerDataListener( DataListener listener ) { mDataListeners.registerDataListener( listener ); }

  // synchronized( mDataListener )
  void unregisterDataListener( DataListener listener ) { mDataListeners.unregisterDataListener( listener ); }

  static boolean mCosurvey = false;       // whether co-survey is enable by the DB
  static boolean mCoSurveyServer = false; // whether co-survey server is on
  static ConnectionHandler mSyncConn = null;

  // used by TDSetting
  void setCoSurvey( boolean co_survey ) // FIXME interplay with TDSetting
  {
    if ( ! mCosurvey ) {
      mCoSurveyServer = false;
      TDPrefHelper.update( "DISTOX_COSURVEY", false );
      return;
    } 
    mCoSurveyServer = co_survey;
    if ( mCoSurveyServer ) { // start server
      startRemoteTopoDroid( );
    } else { // stop server
      stopRemoteTopoDroid( );
    }
  }

  static int getConnectionType() 
  {
    return ( mSyncConn == null )? SyncService.STATE_NONE : mSyncConn.getType();
  }

  static int getAcceptState()
  {
    return ( mSyncConn == null )? SyncService.STATE_NONE : mSyncConn.getAcceptState();
  }

  static int getConnectState()
  {
    return ( mSyncConn == null )? SyncService.STATE_NONE : mSyncConn.getConnectState();
  }

  static String getConnectionStateStr()
  {
    return ( mSyncConn == null )? "NONE": mSyncConn.getConnectStateStr();
  }

  static String getConnectedDeviceName()
  {
    return ( mSyncConn == null )? null : mSyncConn.getConnectedDeviceName();
  }

  static String getConnectionStateTitleStr()
  {
    return ( mSyncConn == null )? TDString.EMPTY : mSyncConn.getConnectionStateTitleStr();
  }

  static void connStateChanged()
  {
    // TDLog.v( "connStateChanged()" );
    if ( mSurveyWindow != null ) mSurveyWindow.setTheTitle();
    if ( mShotWindow  != null) mShotWindow.setTheTitle();
    if ( mMainActivity != null ) mMainActivity.setTheTitle();
  }

  static void connectRemoteTopoDroid( BluetoothDevice device )
  { 
    if ( mSyncConn != null ) mSyncConn.connect( device );
  }

  void disconnectRemoteTopoDroid( BluetoothDevice device )
  { 
    if ( mSyncConn != null ) {
      unregisterDataListener( mSyncConn );
      mSyncConn.disconnect( device );
    }
  }

  static void syncRemoteTopoDroid( BluetoothDevice device )
  { 
    if ( mSyncConn != null ) mSyncConn.syncDevice( device );
  }

  static void startRemoteTopoDroid( )
  { 
    if ( mSyncConn != null ) mSyncConn.start( );
  }

  void stopRemoteTopoDroid( )
  { 
    if ( mSyncConn != null ) {
      unregisterDataListener( mSyncConn );
      mSyncConn.stop( );
    }
  }

  static void syncConnectionFailed()
  {
    TDToast.makeBad( "Sync connection failed" );
  }

  void syncConnectedDevice( String name )
  {
    TDToast.make( "Sync connected " + name );
    if ( mSyncConn != null ) registerDataListener( mSyncConn );
  }

  */

  // --------------------------------------------------------------

  void refreshUI()
  {
    if ( mSurveyWindow != null ) mSurveyWindow.updateDisplay();
    if ( mShotWindow  != null) mShotWindow.updateDisplay();
    if ( mMainActivity != null ) mMainActivity.updateDisplay();
  }

  void clearSurveyReferences()
  {
    mSurveyWindow = null;
    mShotWindow   = null;
  }

  // ---------------------------------------------------------------
  // DISTOX PAIRING
  // cannot be static because register/unregister are not static

  // active pairing request
  static PairingRequest mPairingRequest = null;

  public static void checkAutoPairing()
  {
    if ( thisApp == null ) return;
    if ( TDSetting.mAutoPair ) {
      thisApp.startPairingRequest();
    } else {
      thisApp.stopPairingRequest();
    }
  }

  /** terminate a pairing request, if any
   */
  void stopPairingRequest()
  {
    if ( mPairingRequest != null ) {
      // TDLog.v( "stop pairing" );
      unregisterReceiver( mPairingRequest );
      mPairingRequest = null;
    }
  }

  /** start a pairing request, if none is ongoing
   */
  private void startPairingRequest()
  {
    if ( mPairingRequest == null ) {
      // TDLog.v( "start pairing" );
      // IntentFilter filter = new IntentFilter( DeviceUtil.ACTION_PAIRING_REQUEST );
      IntentFilter filter = new IntentFilter( "android.bluetooth.device.action.PAIRING_REQUEST" );
      // filter.addCategory( Intent.CATEGORY_ALTERNATIVE );
      mPairingRequest = new PairingRequest();
      registerReceiver( mPairingRequest, filter );
    }
  }

  // ==================================================================
  
  /**
   * @param context         conetxt
   * @param uri             output URI
   * @param exportIndex     export type
   * @param exportPrefix    stationn names export prefix (Compass, VTopo, Winkarst
   * @param toast           whether to toast a message
   * @note called by (ShotWindow and) SurveyWindow on export
   */
  boolean doExportDataAsync( Context context, Uri uri, int exportIndex, String exportPrefix, boolean toast )
  {
    if ( exportIndex < 0 ) return false; // extra safety
    if ( exportIndex == TDConst.SURVEY_FORMAT_ZIP ) { // EXPORT ZIP
      // this is SurveyWindow.doArchive
      while ( ! TopoDroidApp.mEnableZip ) Thread.yield();
      (new ExportZipTask( context, this, uri )).execute();
    } else {
      SurveyInfo info = getSurveyInfo( );
      if ( info == null ) return false;
      // TDLog.v( "APP async-export survey " + TDInstance.survey + " Index " + exportIndex );
      String format = context.getResources().getString(R.string.saved_file_1);
      // if ( ! TDSetting.mExportUri ) uri = null; // FIXME_URI
      (new SaveDataFileTask( uri, format, TDInstance.sid, info, mData, TDInstance.survey, TDInstance.getDeviceA(), exportIndex, exportPrefix, toast )).execute();
    }
    return true;
  }

  // called by zip archiver to export survey data before zip archive if TDSetting.mExportShotFormat >= 0
  // static void doExportDataSync( int exportType )
  // {
  //   if ( exportType < 0 ) return;
  //   if ( TDInstance.sid >= 0 ) {
  //     SurveyInfo info = getSurveyInfo( );
  //     if ( info == null ) return;
  //     TDLog.Log( TDLog.LOG_IO, "sync-export survey " + TDInstance.survey + " type " + exportType );
  //     // String saving = null; // because toast is false
  //     (new SaveDataFileTask( null, TDInstance.sid, info, mData, TDInstance.survey, TDInstance.getDeviceA(), exportType, false )).immed_exec();
  //   }
  // }

  // ==================================================================
  // PATH_11

  // static void setPath11NoAgain()
  // {
  //   if ( mDData != null ) mDData.setValue( "Path11", "1" );
  // }

  // static boolean hasPath11NoAgain()
  // {
  //   if ( mDData != null ) {
  //     String no_again = mDData.getValue( "Path11" );
  //     return ( no_again != null && no_again.equals("1") );
  //   }
  //   return false;
  // }

}
