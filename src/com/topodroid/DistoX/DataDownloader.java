/* @file DataDownloader.java
 *
 * @author marco corvi
 * @date sept 2014
 *
 * @brief TopoDroid survey shots data downloader (continuous mode)
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.prefs.TDSetting;
import com.topodroid.dev.ConnectionState;
import com.topodroid.dev.DeviceUtil;

// import java.util.ArrayList;

import android.content.Context;


public class DataDownloader
{
  // int mStatus = 0; // 0 disconnected, 1 connected, 2 connecting

  // private Context mContext; // UNUSED
  // private BroadcastReceiver mBTReceiver = null;
  private final TopoDroidApp mApp;


  private int mConnected = ConnectionState.CONN_DISCONNECTED;  // whether it is "connected": 0 unconnected, 1 connecting, 2 connected
  private boolean mDownload  = false;  // whether it is "downloading"

  boolean isConnected() { return mConnected == ConnectionState.CONN_CONNECTED; }
  boolean isDownloading() { return mDownload; }
  boolean needReconnect() { return mDownload && mConnected != ConnectionState.CONN_CONNECTED; }
  public void setConnected( int connected ) { mConnected = connected; }
  void setDownload( boolean download ) { mDownload = download; }

  /** update the "connected" state
   * @param on_off   whether the downloader is connected
   */
  public void updateConnected( boolean on_off )
  {
    if ( on_off ) {
      mConnected = ConnectionState.CONN_CONNECTED;
    } else {
      if ( mDownload ) {
        // mConnected = ( TDSetting.isConnectionModeContinuous() && TDSetting.mAutoReconnect )? ConnectionState.CONN_WAITING : ConnectionState.CONN_DISCONNECTED;
        mConnected = ConnectionState.CONN_WAITING; // auto-reconnect is always true
      } else {
        mConnected = ConnectionState.CONN_DISCONNECTED;
      }
    }
  }

  /** @return the status of the downloader
   */
  int getStatus()
  {
    if ( ! mDownload ) return ConnectionState.CONN_DISCONNECTED;
    return mConnected;
    // if ( mConnected == 0 ) {
    //   if ( TDSetting.mAutoReconnect ) return ConnectionState.CONN_WAITING;
    //   return ConnectionState.CONN_DISCONNECTED;
    // } else if ( mConnected == 1 ) {
    //   return ConnectionState.CONN_WAITING;
    // } // else mConnected == 2
    // return ConnectionState.CONN_CONNECTED;
  }

  /** cstr
   * @param context   context (unused)
   * @param app       application
   */
  DataDownloader( Context context, TopoDroidApp app )
  {
    // mContext = context;
    mApp     = app;
    // mBTReceiver = null;
  }

  /** toggle the "download" status
   * @return the new download status
   */
  boolean toggleDownload()
  {
    mDownload = ! mDownload;
    mConnected = mDownload ? ConnectionState.CONN_WAITING : ConnectionState.CONN_DISCONNECTED;
    return mDownload;
    // TDLog.v( "toggle download to " + mDownload );
  }

  /** download data
   * @param data_type    expected type of the data
   */
  void doDataDownload( int data_type )
  {
    // TDLog.v( "Data Downloader: do Data Download() - nr " + mDownload + " connected " + mConnected );
    if ( mDownload ) {
      startDownloadData( data_type );
    } else {
      stopDownloadData();
    }
  }

  /** start to download data
   * @param data_type    expected type of the data
   * @note called with mDownload == true
   */
  private void startDownloadData( int data_type )
  {
    // TDLog.Log( TDLog.LOG_COMM, "**** download data. status: " + mStatus );
    if ( TDInstance.isContinuousMode() ) {
      if ( TDSetting.mAutoReconnect ) {
        // TDLog.v( "Data Downloader: start download continuous - autoreconnect ");
        TDInstance.secondLastShotId = TopoDroidApp.lastShotId( ); // FIXME-LATEST
        new ReconnectTask( this, data_type, 0 ).execute();
      } else {
        notifyConnectionStatus( ConnectionState.CONN_WAITING );
        tryConnect( data_type );
      }
    } else if ( TDSetting.isConnectionModeBatch() ) {
      tryDownloadData( data_type );
    } else if ( TDSetting.isConnectionModeMulti() ) {
      tryDownloadData( data_type );
    }
  }

  /** stop to download data
   */
  void stopDownloadData()
  {
    // TDLog.v( "stop Download Data() connected " + mConnected );
    // if ( ! mConnected ) return;
    // if ( TDSetting.isConnectionModeBatch() ) {
      if ( mApp.disconnectComm() ) {
        notifyConnectionStatus( ConnectionState.CONN_DISCONNECTED );
      }
    // }
  }

  /** try to connect to data device
   * @param data_type    expected type of the data
   * @note called also by ReconnectTask
   */
  void tryConnect( int data_type )
  {
    // TDLog.v( "Data Downloader: try Connect() download " + mDownload + " connected " + mConnected );
    if ( TDInstance.getDeviceA() != null && DeviceUtil.isAdapterEnabled() ) {
      mApp.disconnectComm();
      if ( ! mDownload ) {
        mConnected = ConnectionState.CONN_DISCONNECTED;
        return;
      }
      if ( mConnected == ConnectionState.CONN_CONNECTED ) {
        mConnected = ConnectionState.CONN_DISCONNECTED;
        // TDLog.v( "**** toggle: connected " + mConnected );
      } else {
        // if this runs the RFcomm thread, it returns true
        int connected = TDSetting.mAutoReconnect ? ConnectionState.CONN_WAITING : ConnectionState.CONN_DISCONNECTED;

        if ( mApp.connectDevice( TDInstance.deviceAddress(), data_type ) ) {
          connected = ConnectionState.CONN_CONNECTED;
        }
        // TDLog.v( "Data Downloader: **** connect device returns " + connected );
        if ( TDInstance.isDeviceBLE() && connected == ConnectionState.CONN_CONNECTED ) {
          mConnected = connected;
          mApp.notifyStatus( ConnectionState.CONN_WAITING );
        } else {
          notifyUiThreadConnectionStatus( connected );
        }
      }
    }
  }

  /** set the "connected" status and notify the UI 
   * @param connected  new connected status
   */
  private void notifyUiThreadConnectionStatus( int connected )
  {
    mConnected = connected;
    mApp.notifyStatus( getStatus() ); // this is run on UI thread
  }

  /** set the "connected" status and notify the UI 
   * @param connected  new connected status
   * @note this must be called on UI thread (onPostExecute)
   */
  void notifyConnectionStatus( int connected )
  {
    mConnected = connected;
    mApp.notifyStatus( getStatus() ); // this is run on UI thread
  }

  /** batch on-demand download
   * @param data_type   packet datatype
   */
  private void tryDownloadData( int data_type )
  {
    TDInstance.secondLastShotId = TopoDroidApp.lastShotId( ); // FIXME-LATEST
    if ( TDInstance.getDeviceA() != null && DeviceUtil.isAdapterEnabled() ) {
      notifyConnectionStatus( ConnectionState.CONN_WAITING );
      // TDLog.Log( TDLog.LOG_COMM, "shot menu DOWNLOAD" );
      // TDLog.v( "DataDownloader: try Download Data() - type " + data_type );
      new DataDownloadTask( mApp, mApp.mListerSet, null, data_type ).execute();
    } else {
      mDownload = false;
      notifyConnectionStatus( ConnectionState.CONN_DISCONNECTED );
      TDLog.Error( "download data: no device selected" );
      // if ( TDInstance.sid < 0 ) {
      //   TDLog.Error( "download data: no survey selected" );
      // // } else {
      //   // DBlock last_blk = mApp.mData.selectLastLegShot( TDInstance.sid );
      //   // (new ShotNewDialog( mContext, mApp, lister, last_blk, -1L )).show();
      // }
    }
  }

  // static boolean mConnectedSave = false;

  /** lifecycle: on stop
   */
  void onStop()
  {
    // TDLog.v( "DataDownloader onStop()");
    mDownload = false;
    if ( mConnected  > ConnectionState.CONN_DISCONNECTED ) { // mConnected == ConnectionState.CONN_CONNECTED || mConnected == ConnectionState.CONN_WAITING
      stopDownloadData();
      mConnected = ConnectionState.CONN_DISCONNECTED;
    }
  }

  /** lifecycle: on resume (empty method)
   */
  void onResume()
  {
    // TDLog.v( "Data Downloader onResume()");
  }

}
