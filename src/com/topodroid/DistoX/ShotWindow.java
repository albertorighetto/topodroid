/* @file ShotWindow.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid survey shots management
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDMath;
// import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFeedback;
import com.topodroid.utils.TDTag;
import com.topodroid.utils.TDString;
import com.topodroid.utils.TDStatus;
import com.topodroid.utils.TDRequest;
import com.topodroid.utils.TDLocale;
// import com.topodroid.utils.TDVersion;
import com.topodroid.math.TDMatrix;
import com.topodroid.math.TDVector;
import com.topodroid.ui.MyButton;
import com.topodroid.ui.MyTurnBitmap;
import com.topodroid.ui.MyHorizontalButtonView;
import com.topodroid.ui.MyHorizontalListView;
import com.topodroid.help.HelpDialog;
import com.topodroid.help.UserManualActivity;
import com.topodroid.prefs.TDSetting;
import com.topodroid.prefs.TDPrefCat;
import com.topodroid.dev.ConnectionState;
// import com.topodroid.dev.Device;
import com.topodroid.dev.DeviceUtil;
import com.topodroid.dev.DataType;
import com.topodroid.common.PlotType;
import com.topodroid.common.LegType;
import com.topodroid.common.ExtendType;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
// import java.util.Stack;
import java.util.Locale;

import android.os.Bundle;
import android.os.Handler;
// /* fixme-23 */
// import java.lang.reflect.Method;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;

import android.view.View;
// import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;

import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.EditText;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;


// import android.provider.MediaStore;
// import android.provider.Settings.System;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

// import android.net.Uri;

// FIXME-28
// import androidx.recyclerview.widget.RecyclerView;
// import androidx.recyclerview.widget.LinearLayoutManager;

public class ShotWindow extends Activity
                        implements OnItemClickListener
                        , OnItemLongClickListener
                        , OnClickListener
                        , OnLongClickListener
                        , ILister
                        , INewPlot
                        , IPhotoInserter
{
  final static private int BTN_DOWNLOAD  = 0;
  final static private int BTN_BLUETOOTH = 1;
  final static private int BTN_PLOT      = 3;
  final static private int BTN_MANUAL    = 5;
  final static private int BTN_AZIMUTH   = 6;
  final static private int BTN_SEARCH    = 8;
  private int boff = 0;
  private int mBTstatus; // status of bluetooth buttons (download and reset)

  private DataHelper mApp_mData;

  private static final int[] izons = {
                        R.drawable.iz_download,
                        R.drawable.iz_bt,
                        R.drawable.iz_mode,
                        R.drawable.iz_plot,
                        R.drawable.iz_note,
                        R.drawable.iz_plus,
                        R.drawable.iz_dial,
                        R.drawable.iz_station,
                        R.drawable.iz_search,
			R.drawable.iz_refresh,
			R.drawable.iz_empty
                      };

  private static final int[] izonsno = {
                        0,
                        0,
                        0,
                        R.drawable.iz_plot, // TODO_IZ
                        0,
                        0,
                        0
                      };

  private static final int[] izonsF = {
                        R.drawable.iz_left,       // extend LEFT
                        R.drawable.iz_flip,       // extend flip
                        R.drawable.iz_right,      // extend RIGHT
                        R.drawable.iz_highlight,  // multishot dialog: includes // highlight in sketch
                          // R.drawable.iz_highlight,  // highlight in sketch
			  // R.drawable.iz_numbers_no, // renumber shots (first must be leg)
                          // R.drawable.iz_bedding,    // compute bedding
                        R.drawable.iz_delete,     // cut shots 
                        R.drawable.iz_copy,       // copy shots 
                        R.drawable.iz_cancel,     // cancel
			R.drawable.iz_empty
                      };
  private static final int BTN_HIGHLIGHT = 3; // index of iz_highlight
  private static final int BTN_COPY      = 5; // index of iz_copy

  private static final int[] menus = { // menu labels
                        R.string.menu_close,
                        R.string.menu_survey,
                        R.string.menu_recover,
                        R.string.menu_photo,
                        R.string.menu_audio,
                        R.string.menu_sensor,
                        R.string.menu_3d,
                        R.string.menu_device,
                        R.string.menu_options,
                        R.string.menu_help,
                        R.string.menu_recover_paste  // 10: extra labels
                     };

  private static final int[] help_icons = {
                          R.string.help_download,
                          R.string.help_remote,
                          R.string.help_display,
                          R.string.help_plot,
                          R.string.help_note,
                          R.string.help_add_shot,
                          R.string.help_azimuth,
                          R.string.help_current_station,
                          R.string.help_search,
			  R.string.help_refresh,
                        };
   private static final int[] help_menus = {
                          R.string.help_close,
                          R.string.help_survey_info,
                          R.string.help_undelete,
                          R.string.help_photo,
                          R.string.help_audio,
                          R.string.help_sensor,
                          R.string.help_3d,
                          R.string.help_device,
                          R.string.help_prefs,
                          R.string.help_help
                      };

  private static final int HELP_PAGE = R.string.ShotWindow;

  private TopoDroidApp   mApp;
  private Activity       mActivity;
  private DataDownloader mDataDownloader;
  private SurveyAccuracy mSurveyAccuracy;

  private static DBlockBuffer mDBlockBuffer = new DBlockBuffer(); // survey-data buffer (created on first call)

  // TODO replace flags with DisplayMode-flag 
  //      N.B. id is in the data adapter
  private boolean mFlagSplay  = true;  //!< whether to hide splay shots
  private boolean mFlagLatest = false; //!< whether to show the latest splay shots
  private boolean mFlagLeg    = true;  //!< whether to hide leg extra shots
  private boolean mFlagBlank  = false; //!< whether to hide blank shots

  boolean isFlagSplay()  { return mFlagSplay; }
  boolean isFlagLatest() { return mFlagLatest; }
  boolean isFlagLeg()    { return mFlagLeg; }
  boolean isFlagBlank()  { return mFlagBlank; }
  boolean isShowIds()    { return mDataAdapter.show_ids; }

  // void setShowIds( boolean show ) { mDataAdapter.show_ids = show; }

  void setFlags( boolean ids, boolean splay, boolean latest, boolean leg, boolean blank )
  {
    mDataAdapter.show_ids = ids;
    mFlagSplay = splay;
    if ( TDSetting.mShotRecent ) mFlagLatest = latest;
    mFlagLatest = latest;
    mFlagLeg    = leg;
    mFlagBlank  = blank;
    saveInstanceToData();
    updateDisplay();
  }



  // private Bundle mSavedState = null;
  // private String mRecentPlot     = null; // moved to TDInstance
  // long   mRecentPlotType = PlotType.PLOT_PLAN;

  private int mButtonSize = 42;
  private Button[] mButton1;
  private int mNrButton1 = 0;

  private ListView mList;
  // private int mListPos = -1;
  // private int mListTop = 0;
  private DBlockAdapter mDataAdapter;
  // private ArrayAdapter< String > mDataAdapter;
  private ArrayList< String > mShowSplay;


  // private long mLastExtend; // id of the last-extend-ed splay 

  // private static final String LIST_STATE = "listState";
  // private int mFirstPos = -1;  
  // private int mScroll   = 0;
  // private int mSavePos  = -1;  // shot entry position
  private int mShotPos  = -1;  // shot entry position
  private int mPrevPos  = 0;   // prev shot entry position
  private int mNextPos  = 0;   // next shot entry position
  // private TextView mSaveTextView = null;

  private static long    mSensorId;
  // private static long    mPhotoId;
  // private static String  mPhotoComment;
  // private static int     mPhotoCamera = PhotoInfo.CAMERA_UNDEFINED;
  private static long    mShotId;   // photo/sensor shot id, old shot id
  boolean mOnOpenDialog = false;
  MediaManager mMediaManager;

  // ConnHandler mHandler;

  private Button[] mButtonF;
  private int mNrButtonF = 7; // 8;

  // private Button mButtonHelp;
  private MyHorizontalListView mListView;
  private MyHorizontalButtonView mButtonView1;
  private MyHorizontalButtonView mButtonViewF;
  private ListView   mMenu = null;
  private Button     mMenuImage;
  private boolean onMenu = false;
  private boolean onMultiselect = false;

  private BitmapDrawable mBMbluetooth;
  private BitmapDrawable mBMbluetooth_no;
  private BitmapDrawable mBMdownload;
  private BitmapDrawable mBMdownload_on;
  private BitmapDrawable mBMdownload_wait;
  private BitmapDrawable mBMdownload_no;
  // BitmapDrawable mBMadd;
  private BitmapDrawable mBMplot;
  Bitmap mBMdial; // FIXME_AZIMUTH_DIAL
  // Bitmap mBMdial_transp;
  private MyTurnBitmap mDialBitmap;

  private BitmapDrawable mBMplot_no;
  private BitmapDrawable mBMleft;
  private BitmapDrawable mBMright;
  // private SearchResult mSearch = null;

  boolean isBlockMagneticBad( DBlock blk ) { return mSurveyAccuracy.isBlockAMDBad( blk ); }

  String getBlockExtraString( DBlock blk ) { return mSurveyAccuracy.getBlockExtraString( blk ); }

  // --------------------------------------------------------------------------------
  // get a button-1
  // @param idx    index of the button-1
  private Button button1( int idx ) { return mButton1[ idx - boff ]; }

  // check if a button is a given button-1
  // @param b    button
  // @param idx  index of button-1
  private boolean isButton1( Button b, int idx ) { return idx - boff < mNrButton1 && b == button1( idx ); }

  // --------------------------------------------------------------------------------
  public void setRefAzimuth( float azimuth, long fixed_extend )
  {
    // TDLog.v( "set Ref Azimuth " + fixed_extend + " " + azimuth );
    TDAzimuth.mFixedExtend = fixed_extend;
    TDAzimuth.mRefAzimuth  = azimuth;
    setRefAzimuthButton();
  }

  public void setRefAzimuthButton()
  {
    if ( ! TDLevel.overNormal ) return;
    if ( BTN_AZIMUTH - boff >= mNrButton1 ) return;

    // TDLog.v( "set RefAzimuthButton extend " + TDAzimuth.mFixedExtend + " " + TDAzimuth.mRefAzimuth );
    if ( TDAzimuth.mFixedExtend == 0 ) {
      // FIXME_AZIMUTH_DIAL 2
      // android.graphics.Matrix m = new android.graphics.Matrix();
      // m.postRotate( TDAzimuth.mRefAzimuth - 90 );

      // if ( mBMdial != null ) // extra care !!!
      {
        int extend = (int)(TDAzimuth.mRefAzimuth);
        // FIXME_AZIMUTH_DIAL 2
        // Bitmap bm1 = Bitmap.createScaledBitmap( mBMdial, mButtonSize, mButtonSize, true );
        // Bitmap bm2 = Bitmap.createBitmap( bm1, 0, 0, mButtonSize, mButtonSize, m, true);
	Bitmap bm2 = mDialBitmap.getBitmap( extend, mButtonSize );

        TDandroid.setButtonBackground( button1( BTN_AZIMUTH ), new BitmapDrawable( getResources(), bm2 ) );
        TopoDroidApp.setSurveyExtend( extend );
      }
    } else if ( TDAzimuth.mFixedExtend == -1L ) {
      TDandroid.setButtonBackground( mButton1[ BTN_AZIMUTH - boff ], mBMleft );
      TopoDroidApp.setSurveyExtend( SurveyInfo.SURVEY_EXTEND_LEFT );
    } else {
      TDandroid.setButtonBackground( mButton1[ BTN_AZIMUTH - boff ], mBMright );
      TopoDroidApp.setSurveyExtend( SurveyInfo.SURVEY_EXTEND_RIGHT );
    } 
  }

  TopoDroidApp getApp() { return mApp; }

  Set<String> getStationNames() { return mApp_mData.selectAllStations( TDInstance.sid ); }

  // -------------------------------------------------------------------
  // FIXME ok only for numbers
  // String getNextStationName()
  // {
  //   return mApp_mData.getNextStationName( TDInstance.sid );
  // }

  @Override
  public void refreshDisplay( int nr, boolean toast ) 
  {
    // TDLog.v("SHOT refresh display " + nr );
    setConnectionStatus( mDataDownloader.getStatus() );
    if ( nr >= 0 ) {
      if ( nr > 0 ) {
        // mLastShotId = mApp_mData.getLastShotId( TDInstance.sid );
        updateDisplay( );
      }
      if ( toast ) {
        if ( TDInstance.isDeviceX310() ) nr /= 2;
        TDToast.make( getResources().getQuantityString(R.plurals.read_data, nr, nr ) );
        // TDToast.make( " read_data: " + nr );
      }
    } else { // ( nr < 0 )
      if ( toast ) {
        if ( nr <= -5 ) {
          TDToast.makeBad( getString(R.string.read_fail_with_code) + nr );
	} else {
          TDToast.makeBad( mApp.DistoXConnectionError[ -nr ] );
        }
      }
    }
  }
    
  /** update the display 
   */
  void updateDisplay( )
  {
    // TDLog.v("SHOT update display");
    // highlightBlocks( null );
    if ( mApp_mData != null && TDInstance.sid >= 0 ) {
      List< DBlock > list = mApp_mData.selectAllShots( TDInstance.sid, TDStatus.NORMAL );
      mSurveyAccuracy = new SurveyAccuracy( list ); 
      // if ( list.size() > 4 ) SurveyAccuracy.setBlocks( list );

      List< PhotoInfo > photos = mApp_mData.selectAllPhotos( TDInstance.sid, TDStatus.NORMAL );
      updateShotList( list, photos );
      
      setTheTitle( );
    } else {
      mApp.clearSurveyReferences();
      doFinish();
      // TDToast.makeWarn( R.string.no_survey );
    }
  }


  public void setTheTitle()
  {
    StringBuilder sb = new StringBuilder();
    if ( TDSetting.isConnectionModeMulti() /* || TDSetting.isConnectionModeDouble() */ ) {
      sb.append( "{" );
      if ( TDInstance.getDeviceA() != null ) sb.append( TDInstance.getDeviceA().getNickname() );
      sb.append( "} " );
    }
    // sb.append( mApp.getConnectionStateTitleStr() ); // IF_COSURVEY
    sb.append( TDInstance.survey );

    setTitleColor( StationPolicy.mTitleColor );
    mActivity.setTitle( sb.toString() );
  }

  boolean isCurrentStationName( String st ) { return mApp.isCurrentStationName( st ); }

  boolean setCurrentStationName( String st ) 
  { 
    mSkipItemClick = true;
    return mApp.setCurrentStationName( st ); 
    // updateDisplay( );
  }

  // add a block to the adapter (ILister interface)
  // called by the RFcommThread after receiving a data packet
  // this is the only place where assignStationsAll is called: 
  // synchronize it
  @Override
  synchronized public void updateBlockList( long blk_id )
  {
    DBlock blk = mApp_mData.selectLastShot( blk_id, TDInstance.sid );
    if ( blk == null || mDataAdapter == null ) {
      // TDLog.v("DATA " + "null block");
      return;
    }
    // TDLog.v("DATA " + "Shot window: update block list. Id: " + blk_id + " is scan: " +  blk.isScan() );
    // FIXME 3.3.0
    boolean add_block = mDataAdapter.addDataBlock( blk ); // avoid double block-adding
    if ( add_block ) {
      boolean scan = blk.isScan();
      boolean ret = false;
      if ( ! scan ) { // normal data
        mSurveyAccuracy.addBlockAMD( blk );
        if ( StationPolicy.doBacksight() || StationPolicy.doTripod() ) {
          ret = mApp.assignStationsAll( mDataAdapter.getItems() );
        } else {
          ret = mApp.assignStationsAll( mDataAdapter.getItemsForAssign() );
        }
        // mApp_mData.getShotName( TDInstance.sid, blk );
        // TDLog.v("DATA " + "shot window block " + blk.mId + " station assign return " + ret );
      }

      mList.post( new Runnable() {
        @Override public void run() {
          // TDLog.v( "shot window notify data set changed " + mDataAdapter.getCount() );
          mDataAdapter.notifyDataSetChanged(); // THIS IS IMPORTANT TO REFRESH THE DATA LIST
          mList.setSelection( mDataAdapter.getCount() - 1 );
        }
      } );
      // mList.invalidate();
      // mDataAdapter.reviseLatest();
      if ( ret || scan ) { // always update when a leg is received 
        // TDLog.v("DATA " + "shot window got a leg. ret " + ret );
        TopoDroidApp.notifyDrawingUpdateDisplay( blk_id, ret );
      } else if ( ! StationPolicy.isSurveyBackward1() ) {
        if ( TDLevel.overExpert || ! TDSetting.mLegOnlyUpdate ) {
          TopoDroidApp.notifyDrawingUpdateDisplay( blk_id, ret );
        }
      }
    // } else {
    //   TDLog.v( "block " + blk_id + " already on the list");
    }
  }

  /** update the list of shot data with the photo infos
   * @param list    list of shot data
   * @param photos  list of photos
   * first clear the data adapter, then fill it with the shot data, finally with the photo infos
   * @note called only by updateDisplay
   */
  private void updateShotList( List< DBlock > list, List< PhotoInfo > photos )
  {
    // TDLog.Log( TDLog.LOG_SHOT, "updateShotList shots " + list.size() + " photos " + photos.size() );
    mDataAdapter.clear();
    // mList.setAdapter( mDataAdapter );
    if ( list.size() == 0 ) {
      // TDToast.makeWarn( R.string.no_shots );
      return;
    }
    processShotList( list );
    mDataAdapter.reviseBlockWithPhotos( photos );
  }

  /** process the list of shot data and fill the adapter for the display list
   * @param list    list of shot data
   * this is a bit of overkill because the shot leg type are updated in the database
   * @note called only by updateShotList
   */
  private void processShotList( List< DBlock > list )
  {
    // TDLog.v( "process shot list");
    DBlock prev = null;
    boolean prev_is_leg = false;
    boolean check_recent = TDSetting.mShotRecent && mFlagLatest;
    for ( DBlock item : list ) {
      DBlock cur = item;
      // int t = cur.type();
      // TDLog.Log( TDLog.LOG_SHOT, "item " + cur.mLength + " " + cur.mBearing + " " + cur.mClino );

      if ( cur.isSecLeg() || cur.isRelativeDistance( prev ) ) {
        // TDLog.v( "item close " + cur.type() + " " + cur.mLength + " " + cur.mBearing + " " + cur.mClino );
        if ( cur.isBlank() ) {   // FIXME 20140612
          cur.setTypeSecLeg();
          mApp_mData.updateShotLeg( cur.mId, TDInstance.sid, LegType.EXTRA ); // cur.mType ); // FIXME 20140616
        }
        else if ( ! cur.isSecLeg() ) { // FIXME 20201118
          // if ( prev != null && prev.isBlank() ) prev.setBlockType( DBlock.BLOCK_BLANK_LEG );
          if ( prev != null ) prev.setTypeBlankLeg();
        }

        if ( mFlagLeg ) { // flag: hide leg extra shots
          // TDLog.Log( TDLog.LOG_SHOT, "close distance");

          if ( mFlagBlank && prev != null && prev.isTypeBlank() ) {
            // prev was skipped: draw it now
            if ( ! prev_is_leg ) {
              cur = prev;
              prev_is_leg = true;
            } else {
              continue;
            }
          } else {
            continue;
          }
        } else { // do not hide extra leg-shots
          if ( mFlagBlank && prev != null && prev.isTypeBlank() ) {
            if ( ! prev_is_leg ) {
              mDataAdapter.add( prev );
              prev_is_leg = true;
            // } else {
              /* nothing */
            }
          // } else {
          //   /* nothing */
          }
        }
      } else {
        // TDLog.v( "item not close " + cur.type() + " " + cur.mLength + " " + cur.mBearing + " " + cur.mClino );
        // TDLog.Log( TDLog.LOG_SHOT, "not close distance");
        prev_is_leg = false;
        if ( cur.isTypeBlank() ) { // DBlock.isTypeBlank(t)
          prev = cur;
          if ( mFlagBlank ) continue;
        } else if ( cur.isSplay() ) { // DBlock.isSplay(t) 
          prev = null;
          if ( mFlagSplay ) { // do hide splays, except those that are shown.
            // boolean skip = true;
            // for ( String st : mShowSplay ) {
            //   if ( st.equals( cur.mFrom ) ) { skip = false; break; }
            // }
            // if ( skip ) continue;
            if ( ! ( showSplaysContains( cur.mFrom ) || ( check_recent && cur.isRecent() ) ) ) continue;
          }
        } else { // cur.isMainLeg() // t == DBlock.BLOCK_MAIN_LEG
          prev = cur;
        }
      }
      // TDLog.Log( TDLog.LOG_SHOT, "adapter add " + cur.mLength + " " + cur.mBearing + " " + cur.mClino );
      // TDLog.v( "shot adapter add " + cur.mLength + " " + cur.mBearing + " " + cur.mClino );
      mDataAdapter.add( cur );
    }
  }

  // ---------------------------------------------------------------
  // list items click

  private boolean mSkipItemClick = false;

  /** begin multiselection at a given position
   * @param pos   item position
   * @note the multiselection is closed if it contains only the given position
   */
  private void multiSelect( int pos )
  {
    // TDLog.v("multiselect " + pos );
    if ( mDataAdapter.multiSelect( pos ) ) {
      mListView.setAdapter( mButtonViewF.mAdapter );
      mListView.invalidate();
      onMultiselect = true;
      // FIXME this should not be neceessary but it is (???)
      if ( pos == 0 ) mDataAdapter.notifyDataSetChanged(); // THIS IS USED TO REFRESH THE DATA LIST
    } else {
      mListView.setAdapter( mButtonView1.mAdapter );
      mListView.invalidate();
      onMultiselect = false;
    }
  }

  /** close the multiselection
   */
  void clearMultiSelect( )
  {
    // mApp.setHighlighted( null ); // FIXME_HIGHLIGHT
    mDataAdapter.clearMultiSelect( );
    mListView.setAdapter( mButtonView1.mAdapter );
    mListView.invalidate();
    onMultiselect = false;
  }

  /** react to a user tap on an item (menu entry)
   * @param parent   ...
   * @param view     tapped view
   * @param pos      item position
   * @param id       ...
   */
  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    // TDLog.v( "on item click " + view.getId() );
    if ( CutNPaste.dismissPopupBT() ) return;
    if ( mSkipItemClick ) {
      mSkipItemClick = false;
      return;
    }
    if ( parent != null && mMenu == (ListView)parent ) {
      handleMenu( pos );
      return;
    }
    if ( closeMenu() ) return;
  }

  /** implement user tap on a shot data
   * @param view  tapped viee
   * @param pos   item position
   *
   * if it is in "multiselection" add the item to the multiselection,
   * otherwise perform an "item click"
   */
  public void itemClick( View view, int pos )
  {
    if ( mDataAdapter.isMultiSelect() ) {
      multiSelect( pos );
      return;
    }
    DBlock blk = mDataAdapter.get(pos);
    if ( blk != null ) onBlockClick( blk, pos );
  }

  /** perform a user tap on a shot data
   * @param blk   item data-block
   * @param pos   item position
   */
  void onBlockClick( DBlock blk, int pos )
  {
    // TDLog.v( "on block click: on_open " + mOnOpenDialog );
    if ( mOnOpenDialog ) return;
    mOnOpenDialog = true;
    mShotPos = pos;
    DBlock prevBlock = null;
    DBlock nextBlock = null;
    prevBlock = getPreviousLegShot( blk, false );
    nextBlock = getNextLegShot( blk, false );
    (new ShotDialog( mActivity, this, pos, blk, prevBlock, nextBlock )).show();
  }

  /** react to a user long-tap on an item (menu entry)
   * @param parent   ...
   * @param view     tapped view
   * @param pos      item position
   * @param id       ...
   */
  @Override 
  public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id)
  {
    // TDLog.v( "on item longClick " + view.getId() );
    if ( closeMenu() ) return true;
    if ( CutNPaste.dismissPopupBT() ) return true;
    return false;
  }

  /** implement a user long-tap of a shot data item: start a multiselection
   * @param view     tapped view
   * @param pos      item position
   */
  public boolean itemLongClick( View view, int pos )
  {
    DBlock blk = mDataAdapter.get(pos);
    if ( blk == null ) return false;
    // onBlockLongClick( blk );
    // if ( blk.isSplay() ) {
    //   highlightBlocks( blk );
    // } else {
      multiSelect( pos );
    // }
    return true;
  }

  // FIXME_X2_SPLAY
  // @param leg0   old leg type
  // @param leg1   new leg type
  void updateSplayLeg( int pos, long leg0, long leg1 ) // pos = mDataAdapter pos
  {
    // TDLog.v( "toggle splays " + leg0 + " -> " + leg1 );
    DBlock blk = mDataAdapter.get(pos);
    if ( blk == null || ! blk.isSplay() ) return;
    do {
      // TDLog.v("toggle splay type " + pos + " is splay " + blk.isSplay() + " leg " + blk.getLegType() );
      blk.setBlockType( (int)leg1 );
      mApp_mData.updateShotLeg( blk.mId, TDInstance.sid, leg1 );
      mDataAdapter.updateBlockView( blk.mId );
      if ( (--pos) < 0 ) break;
      blk = mDataAdapter.get(pos);
    } while ( blk != null && blk.isSplay() && blk.getLegType() == leg0 );
    updateDisplay();
  }

  // assign splay classes traversing backwards: expected
  //   V-splays from +/-90 to -/+90 ... to +/-90
  //   H-splays all in [-30, +30]
  //   X-splays from +/-90 (to -/+90 and againt to +/-90)
  void setSplayClasses( int pos )
  {
    DBlock blk = null;
    do {
      if ( (--pos) < 0 ) return;
      blk = mDataAdapter.get(pos);
    } while ( blk != null && ! blk.isSplay() );
    if ( blk == null ) return;

    int flip = 0;
    int sign = 0;
    long leg1 = LegType.VSPLAY;
    do {
      if ( ! blk.isSplay() ) break;
      switch ( flip ) {
        case 0:
          if ( Math.abs(blk.mClino) < 60 ) { flip = 1; }
          else { sign = ( blk.mClino > 0 ) ? -1 : 1; }
          break;
        case 1:
          if ( sign * blk.mClino > 60 ) { flip = 2; sign = -sign; }
          break;
        case 2:
          if ( sign * blk.mClino > 60 ) { flip = 3; }
          break;
        case 3:
          if ( Math.abs(blk.mClino) < 30 ) { flip = 4; leg1 = LegType.HSPLAY; }
          break;
        case 4:
          if ( Math.abs(blk.mClino) > 60 ) { flip = 5; leg1 = LegType.XSPLAY; }
          break;
      }
      // TDLog.v("toggle splay type " + pos + " is splay " + blk.isSplay() + " leg " + blk.getLegType() );
      blk.setBlockType( (int)leg1 );
      mApp_mData.updateShotLeg( blk.mId, TDInstance.sid, leg1 );
      mDataAdapter.updateBlockView( blk.mId );
      if ( (--pos) < 0 ) break;
      blk = mDataAdapter.get(pos);
    } while ( blk != null && blk.isSplay() );

    updateDisplay();
  }

  // FIXME_X3_SPLAY from ShotDialog
  // @param leg1  new leg type
  void updateSplayLegType( DBlock blk, long leg1 )
  {
    // TDLog.v("update splay " + blk.mId + " leg type " + leg1 );
    int block_type = DBlock.blockOfSplayLegType[ (int)leg1 ];
    // long leg_type = DBlock.legOfBlockType[ block_type ];
    mApp_mData.updateShotLeg( blk.mId, TDInstance.sid, leg1 );
    blk.setBlockType( block_type );
    updateDisplay();
  }

  // from MultiselectDialog
  void updateSplaysLegType( List< DBlock > blks, int leg_type, long flag )
  {
    int block_type = DBlock.blockOfSplayLegType[ leg_type ];
    for ( DBlock blk : blks ) {
      // long leg_type = DBlock.legOfBlockType[ block_type ];
      mApp_mData.updateShotLegFlag( blk.mId, TDInstance.sid, leg_type, flag );
      blk.setBlockType( block_type );
      blk.resetFlag( flag );
    }
    updateDisplay();
    clearMultiSelect();
  }

  // called by ShotDialog "More" button
  void onBlockLongClick( DBlock blk )
  {
    mShotId = blk.mId; // save shot id
    if ( TDLevel.overNormal ) {
      (new PhotoSensorsDialog(mActivity, this, blk ) ).show();
    } else {
      (new ShotDeleteDialog( mActivity, this, blk ) ).show();
    }
  }

  // ----------------------------------------------------------------------------
  // MENU

  private void handleMenu( int pos )
  {
    closeMenu();
    int p = 0;
    if ( p++ == pos ) { // CLOSE
      // if ( TDSetting.mDataBackup ) TopoDroidApp.doExportDataAsync( getApplicationContext(), TDSetting.mExportShotsFormat, false ); // try_save
      TopoDroidApp.mShotWindow = null;
      super.onBackPressed();

    } else if ( p++ == pos ) { // SURVEY ACTIVITY
      Intent intent = new Intent( this, SurveyWindow.class );
      intent.putExtra( TDTag.TOPODROID_SURVEY,  0 ); // mustOpen 
      intent.putExtra( TDTag.TOPODROID_OLDSID, -1 ); // old_sid 
      intent.putExtra( TDTag.TOPODROID_OLDID,  -1 ); // old_id 
      startActivityForResult( intent, TDRequest.INFO_ACTIVITY_SHOTWINDOW );
    // } else if ( TDLevel.overBasic && p++ == pos ) { // CURRENT STATION
    //   (new CurrentStationDialog( this, this, mApp, mApp.getCurrentOrLastStation() )).show();

    } else if ( TDLevel.overBasic && p++ == pos ) { // RECOVER
      List< DBlock > shots1 = mApp_mData.selectAllShots( TDInstance.sid, TDStatus.DELETED );
      List< DBlock > shots2 = mApp_mData.selectAllShots( TDInstance.sid, TDStatus.OVERSHOOT );
      List< DBlock > shots3 = mApp_mData.selectAllShots( TDInstance.sid, TDStatus.CHECK );
      List< PlotInfo > plots   = mApp_mData.selectAllPlots( TDInstance.sid, TDStatus.DELETED );
      // the list of deleted plots contains an even number of items: plan-profile pairs
      if (  TDLevel.overAdvanced ) {
        if ( shots1.size() == 0 && shots2.size() == 0 && shots3.size() == 0 && plots.size() == 0 && mDBlockBuffer.size() == 0 ) {
          TDToast.makeWarn( R.string.no_undelete_paste );
        } else {
          (new UndeleteDialog(mActivity, this, mApp_mData, TDInstance.sid, shots1, shots2, shots3, plots, mDBlockBuffer ) ).show();
        }
      } else {
        if ( shots1.size() == 0 && shots2.size() == 0 && shots3.size() == 0 && plots.size() == 0 ) {
          TDToast.makeWarn( R.string.no_undelete );
        } else {
          (new UndeleteDialog(mActivity, this, mApp_mData, TDInstance.sid, shots1, shots2, shots3, plots, null ) ).show();
        }
      }
      // updateDisplay( );
    } else if ( TDLevel.overNormal && p++ == pos ) { // PHOTO
      // mActivity.startActivity( new Intent( mActivity, PhotoActivity.class ) );
      (new PhotoListDialog( this, mApp_mData )).show();
    } else if ( TDLevel.overExpert && p++ == pos ) { // AUDIO
      List< AudioInfo > audios = mApp_mData.selectAllAudios( TDInstance.sid );
      if ( audios.size() > 0 ) {  
        List< DBlock > shots = mApp_mData.selectAllShots( TDInstance.sid, TDStatus.NORMAL );
        (new AudioListDialog( this, this, audios, shots )).show();
      } else { 
        TDToast.makeWarn( R.string.no_audio );
      }
    } else if ( TDSetting.mWithSensors && TDLevel.overNormal && p++ == pos ) { // SENSORS
      mActivity.startActivity( new Intent( mActivity, SensorListActivity.class ) );
    } else if ( TDLevel.overBasic && p++ == pos ) { // 3D
      // if ( TopoDroidApp.exportSurveyAsThSync( ) ) { // make sure to have survey exported as therion
        // int check = TDVersion.checkCave3DVersion( this );
        // // TDLog.v( "check Cave3D version: " + check );
        // if ( check < 0 ) {
        //   TDToast.makeBad( R.string.no_cave3d );
        // } else if ( check == 0 ) {
          try {
            // FIXME CAVE3D Intent intent = new Intent( "Cave3D.intent.action.Launch" );
            Intent intent = new Intent( Intent.ACTION_VIEW ).setClass( this, com.topodroid.DistoX.TopoGL.class );
            intent.putExtra( "INPUT_SURVEY", TDInstance.survey );
            intent.putExtra( "SURVEY_BASE", TDPath.getPathBase() );
            mActivity.startActivity( intent );
          } catch ( ActivityNotFoundException e ) {
            TDToast.makeBad( R.string.no_cave3d );
          }
        // } else {
        //   TDToast.makeBad( R.string.outdated_cave3d );
        // }
      // }
    } else if ( TDLevel.overNormal && (! TDInstance.isDivingMode()) && p++ == pos ) { // DEVICE
      if ( DeviceUtil.isAdapterEnabled() ) {
        // referrer
        // Intent intent = new Intent( mActivity, com.topodroid.DistoX.DeviceActivity.class );
        // intent.putExtra( TDTag.TOPODROID_DEVICE_REF, Integer.toString( DeviceActivity.REFERRER_SHOT ) );
        // mActivity.startActivity( intent );
        mActivity.startActivity( new Intent( Intent.ACTION_VIEW ).setClass( mActivity, DeviceActivity.class ) );
      }
    } else  if ( p++ == pos ) { // OPTIONS
      Intent intent = new Intent( mActivity, com.topodroid.prefs.TDPrefActivity.class );
      intent.putExtra( TDPrefCat.PREF_CATEGORY, TDPrefCat.PREF_CATEGORY_SURVEY );
      mActivity.startActivity( intent );
    } else if ( p++ == pos ) { // HELP
      // int nn = mNrButton1; //  + ( TDLevel.overNormal ?  mNrButton2 : 0 );
      new HelpDialog(mActivity, izons, menus, help_icons, help_menus, mNrButton1, help_menus.length, getResources().getString( HELP_PAGE ) ).show();
    }
  }

  // ---------------------------------------------------------------

  void askPhotoComment( DBlock blk )
  {
    (new PhotoCommentDialog(mActivity, this, blk.mId ) ).show();
  }

  /**
   * @param comment  photo comment
   * @param camera   camera type: 0 use URL, 1 use TopoDroid
   */
  void doTakePhoto( long sid, String comment, int camera )
  {
    mMediaManager.prepareNextPhoto( sid, comment, camera );

    // imageFile := PHOTO_DIR / surveyId / photoId .jpg
    // TDLog.Log( TDLog.LOG_SHOT, "photo " + imagefile.toString() );
    if ( mMediaManager.isTopoDroidCamera() ) {
      TDLog.v("take photo with TopoDroid");
      new QCamCompass( this, (new MyBearingAndClino( mApp, mMediaManager.getImageFilepath()) ), this, false, false).show();  // false = with_box, false=with_delay
    } else {
      TDLog.v("take photo with Android");
      try {
        Intent intent = new Intent( android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
        if ( intent.resolveActivity( getPackageManager() ) != null ) {
          startActivityForResult( intent, TDRequest.CAPTURE_IMAGE_SHOTWINDOW );
        } else {
          TDToast.makeBad( R.string.no_capture_app );
        }
      } catch ( ActivityNotFoundException e ) {
        TDToast.makeBad( R.string.no_capture_app );
      }
    }
  }

  void askSensor( DBlock blk )
  {
    mSensorId = mApp_mData.nextSensorId( TDInstance.sid );
    // TDLog.Log( TDLog.LOG_SENSOR, "sensor " + mSensorId );
    Intent intent = new Intent( mActivity, SensorActivity.class );
    startActivityForResult( intent, TDRequest.SENSOR_ACTIVITY_SHOTWINDOW );
  }

  // void askExternal( )
  // {
  //   mSensorId = mApp_mData.nextSensorId( TDInstance.sid );
  //   TDLog.Log( TDLog.LOG_SENSOR, "sensor " + mSensorId );
  //   Intent intent = new Intent( this, ExternalActivity.class );
  //   startActivityForResult( intent, TDRequest.EXTERNAL_ACTIVITY );
  // }

  // called to insert a manual shot after a given shot
  void dialogInsertShotAt( DBlock blk )
  {
    (new ShotNewDialog( this, mApp, this, blk, mShotId )).show();
  }

  // insert a manual intermediate leg
  // called by the photo-sensor dialog only
  // return id of inserted leg
  long insertDuplicateLeg( String from, String to, float distance, float bearing, float clino, int extend )
  {
    long id = mApp.insertDuplicateLeg( from, to, distance, bearing, clino, extend );
    if  ( id >= 0L ) updateDisplay( ); 
    return id;
  }

  boolean insertLRUDatStation( long at, String station, float bearing, float clino,
                            String left, String right, String up, String down )
  {
    // TDLog.v("LRUD " + "insert LRUD " + left + "/" + right + "/" + up + "/" + down + " at " + at );
    if ( mApp.insertLRUDatStation( at, station, bearing, clino, left, right, up, down ) < 0L ) return false;
    updateDisplay( ); 
    return true;
  }

  // called by PhotoSensorDialog to split the survey
  // void askSurvey( )
  // {
  //   String new_survey = null; // new survey name
  //   TopoDroidAlertDialog.makeAlert( this, getResources(), R.string.survey_split,
  //     new DialogInterface.OnClickListener() {
  //       @Override
  //       public void onClick( DialogInterface dialog, int btn ) {
  //         doSplitOrMoveSurvey( new_survey );
  //       }
  //   } );
  // }
  void doSplitOrMoveSurvey()
  {
    (new SurveySplitOrMoveDialog( this, this )).show();
  }
  
  void doSplitOrMoveSurvey( String new_survey )
  {
    long old_sid = TDInstance.sid;
    long old_id  = mShotId;
    // TDLog.v( "split survey " + old_sid + " " + old_id + " new " + ((new_survey == null)? "null" : new_survey) );
    if ( TopoDroidApp.mShotWindow != null ) {
      // // if ( TDSetting.mDataBackup ) TopoDroidApp.doExportDataAsync( getApplicationContext(), TDSetting.mExportShotsFormat, false ); // try_save
      TopoDroidApp.mShotWindow.doFinish();
      // TopoDroidApp.mShotWindow = null; // done in doFinish
    }
    if ( TopoDroidApp.mSurveyWindow != null ) {
      TopoDroidApp.mSurveyWindow.finish();
      TopoDroidApp.mSurveyWindow = null;
    }
    if ( new_survey == null ) {
      TopoDroidApp.mMainActivity.startSplitSurvey( old_sid, old_id ); // SPLIT SURVEY
    } else {
      TopoDroidApp.mMainActivity.startMoveSurvey( old_sid, old_id, new_survey ); // MOVE SURVEY
    }
  }

  // @param id   shot id (actually blk.mId)
  // @param blk  block
  // @param status
  // @param leg  if true delete the whole leg shots
  void doDeleteShot( long id, DBlock blk, int status, boolean leg )
  {
    mApp_mData.deleteShot( id, TDInstance.sid, status );
    if ( blk != null && blk.isMainLeg() ) { //  DBlock.BLOCK_MAIN_LEG 
      if ( leg ) {
        for ( ++id; ; ++id ) {
          DBlock b = mApp_mData.selectShot( id, TDInstance.sid );
          if ( b == null || ! b.isSecLeg() ) { // != DBlock.BLOCK_SEC_LEG 
            break;
	  }
          mApp_mData.deleteShot( id, TDInstance.sid, status );
        }
      } else { // set station to next leg shot
        ++id;
        DBlock b = mApp_mData.selectShot( id, TDInstance.sid );
        if ( b != null && b.isSecLeg() ) { //  DBlock.BLOCK_SEC_LEG --> leg-flag = 0
          mApp_mData.updateShotNameAndDataStatus( id, TDInstance.sid, blk.mFrom, blk.mTo, blk.getIntExtend(), blk.getFlag(), 0, blk.mComment, 0 );
          // mApp_mData.updateShotStatus( id, TDInstance.sid, 0 ); // status normal
        }
      }
    }
    updateDisplay( ); 
  }

  // @from PhotoInserter
  public void insertPhoto( )
  {
    // FIXME TITLE has to go
    mApp_mData.insertPhoto( TDInstance.sid, mMediaManager.getPhotoId(), mMediaManager.getShotId(), "", TDUtil.currentDate(), mMediaManager.getComment(), mMediaManager.getCamera() );
    // FIXME NOTIFY ? no
  }

  // void deletePhoto( PhotoInfo photo ) 
  // {
  //   mApp_mData.deletePhoto( TDInstance.sid, photo.id );
  //   File imagefile = TDFile.getFile( mApp.getSurveyJpgFile( Long.toString(photo.id) ) );
  //   try {
  //     imagefile.delete();
  //   } catch ( IOException e ) { }
  // }

  // void refreshList()
  // {
  //   TDLog.v("SHOT " + "refresh display" );
  //   mDataAdapter.notifyDataSetChanged();
  //   mList.invalidate();
  // }

  // ---------------------------------------------------------------
  @Override
  protected void onActivityResult( int reqCode, int resCode, Intent intent )
  {
    // TDLog.Log( TDLog.LOG_DEBUG, "on Activity Result: request " + reqCode + " result " + resCode );
    switch ( reqCode ) {
      case TDRequest.CAPTURE_IMAGE_SHOTWINDOW:
        if ( resCode == Activity.RESULT_OK ) { // RESULT_OK = -1 (0xffffffff)
          // (new PhotoCommentDialog(this, this, mShotId) ).show();
          Bundle extras = intent.getExtras();
          Bitmap bitmap = (Bitmap) extras.get("data");
          mMediaManager.savePhoto( bitmap, 90 ); // compression = 90
        }
        break;
      case TDRequest.SENSOR_ACTIVITY_SHOTWINDOW:
      // case TDRequest.EXTERNAL_ACTIVITY:
        if ( resCode == Activity.RESULT_OK ) {
          Bundle extras = intent.getExtras();
          if ( extras != null ) {
            String type  = extras.getString( TDTag.TOPODROID_SENSOR_TYPE );
            String value = extras.getString( TDTag.TOPODROID_SENSOR_VALUE );
            String comment = extras.getString( TDTag.TOPODROID_SENSOR_COMMENT );
            // TDLog.Log( TDLog.LOG_SENSOR, "insert sensor " + type + " " + value + " " + comment );

            mApp_mData.insertSensor( TDInstance.sid, mSensorId, mShotId, "",
                                  TDUtil.currentDateTime(),
                                  comment,
                                  type,
                                  value );
            // FIXME NOTIFY ? no
          }
        }
        break;
      case TDRequest.INFO_ACTIVITY_SHOTWINDOW:
        if ( resCode == Activity.RESULT_OK ) {
          // if ( TDSetting.mDataBackup ) TopoDroidApp.doExportDataAsync( getApplicationContext(), TDSetting.mExportShotsFormat, false ); // try_save
          doFinish();
        }
        break;
    }
    setRefAzimuthButton( );
  }

  // ---------------------------------------------------------------
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );

    TDandroid.setScreenOrientation( this );

    setContentView( R.layout.shot_activity );
    mApp = (TopoDroidApp) getApplication();
    mApp_mData = TopoDroidApp.mData;
    TopoDroidApp.mShotWindow = this; // FIXME
    mDataDownloader = mApp.mDataDownloader; // new DataDownloader( this, mApp );
    mActivity = this;
    mOnOpenDialog = false;
    mSurveyAccuracy = new SurveyAccuracy( ); 
    mMediaManager   = new MediaManager( mApp_mData );

    // FIXME-28
    // RecyclerView rv = (RecyclerView) findViewById( R.id.recycler_view );
    // LinearLayoutManager lm = new LinearLayoutManager( this );
    // rv.setLayoutManager( lm );

    mShowSplay   = new ArrayList<>();
    if ( TDSetting.mEditableStations ) {
       mDataAdapter = new DBlockAdapter( this, this, R.layout.dblock_row, new ArrayList< DBlock >() );
    } else {
       mDataAdapter = new DBlockAdapter( this, this, R.layout.dblock_row_noedit, new ArrayList< DBlock >() );
    }

    mListView = (MyHorizontalListView) findViewById(R.id.listview);
    mListView.setEmptyPlacholder( true );
    mButtonSize = TopoDroidApp.setListViewHeight( getApplicationContext(), mListView );

    Resources res = getResources();
    mNrButton1 = TDLevel.overExpert ? 10
               : TDLevel.overAdvanced ? 9
               : TDLevel.overNormal ? 7
               : TDLevel.overBasic ?  6 : 5;
    if ( TDInstance.isDivingMode() ) {
      mNrButton1 -= 3;
      boff = 2;
    } else {
      boff = 0;
    }
    mButton1 = new Button[ mNrButton1 + 1 ]; // last is empty-button
    for ( int k=0; k < mNrButton1; ++k ) { 
      int kk = k+boff;
      mButton1[k] = MyButton.getButton( this, this, izons[kk] );
    }
    mButton1[mNrButton1] = MyButton.getButton( this, this, R.drawable.iz_empty );
    // FIXME_AZIMUTH_DIAL 1,2
    mBMdial          = BitmapFactory.decodeResource( res, R.drawable.iz_dial_transp );
    // mBMdial_transp   = BitmapFactory.decodeResource( res, R.drawable.iz_dial_transp );
    mDialBitmap      = MyTurnBitmap.getTurnBitmap( res );

    mBMplot     = MyButton.getButtonBackground( this, res, izons[BTN_PLOT] );
    mBMplot_no  = MyButton.getButtonBackground( this, res, R.drawable.iz_plot_no );
    mBMleft     = MyButton.getButtonBackground( this, res, R.drawable.iz_left );
    mBMright    = MyButton.getButtonBackground( this, res, R.drawable.iz_right );
    if ( ! TDInstance.isDivingMode() ) {
      mBMdownload = MyButton.getButtonBackground( this, res, izons[BTN_DOWNLOAD] );
      mBMbluetooth = MyButton.getButtonBackground( this, res, izons[BTN_BLUETOOTH] );
      mBMdownload_on   = MyButton.getButtonBackground( this, res, R.drawable.iz_download_on );
      mBMdownload_wait = MyButton.getButtonBackground( this, res, R.drawable.iz_download_wait );
      mBMdownload_no   = MyButton.getButtonBackground( this, res, R.drawable.iz_download_no );
      mBMbluetooth_no  = MyButton.getButtonBackground( this, res, R.drawable.iz_bt_no );
    }
    mBTstatus = ConnectionState.CONN_DISCONNECTED;

    if ( TDLevel.overBasic ) {
      if ( ! TDInstance.isDivingMode() ) mButton1[ BTN_DOWNLOAD ].setOnLongClickListener( this );
      mButton1[ BTN_PLOT - boff ].setOnLongClickListener( this );
      mButton1[ BTN_MANUAL - boff ].setOnLongClickListener( this );
      if ( TDLevel.overAdvanced ) {
        mButton1[ BTN_SEARCH - boff ].setOnLongClickListener( this );
      }
    }

    if ( ! TDLevel.overExpert ) mNrButtonF -= 2;
    mButtonF = new Button[ mNrButtonF + 1 ];
    int k0 = 0;
    for ( int k=0; k < mNrButtonF; ++k ) {
      if ( k == BTN_HIGHLIGHT && ! TDLevel.overExpert ) continue; 
      if ( k == BTN_COPY      && ! TDLevel.overExpert ) continue; 
      mButtonF[k0] = MyButton.getButton( this, this, izonsF[k] );
      ++k0;
    }
    mButtonF[mNrButtonF] = MyButton.getButton( this, this, R.drawable.iz_empty );

    // TDAzimuth.resetRefAzimuth( this, 90 );
    setRefAzimuthButton( ); 

    mButtonView1 = new MyHorizontalButtonView( mButton1 );
    mButtonViewF = new MyHorizontalButtonView( mButtonF );
    mListView.setAdapter( mButtonView1.mAdapter );
    onMultiselect = false;

    mList = (ListView) findViewById(R.id.list);
    // view_group.setDescendantFocusbility( ViewGroup.FOCUS_BLOCK_DESCENDANTS ); //  FOCUS_BEFORE_DESCENDANTS FOCUS_AFTER_DESCENDANTS
    mList.setOnScrollListener( new OnScrollListener() {
      @Override public void onScroll( AbsListView listview, int first, int count, int total ) { }

      @Override public void onScrollStateChanged( AbsListView list_view, int state ) { 
        if ( OnScrollListener.SCROLL_STATE_TOUCH_SCROLL == state ) {
          View focus_view = getCurrentFocus();
          if ( focus_view != null ) focus_view.clearFocus();
        }
      }
    } );

    // https://stackoverflow.com/questions/7100555/preventing-catching-illegalargumentexception-parameter-must-be-a-descendant-of
    mList.setRecyclerListener( new AbsListView.RecyclerListener() {
        @Override
        public void onMovedToScrapHeap(View view) {
            if ( view.hasFocus()){
                view.clearFocus(); //we can put it inside the second if as well, but it makes sense to do it to all scraped views
                //Optional: also hide keyboard in that case
                if ( view instanceof EditText) {
                    InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }
    });

    mList.setAdapter( mDataAdapter );
    // mList.setOnItemClickListener( this );
    // mList.setLongClickable( true );
    // mList.setOnItemLongClickListener( this );
    mList.setDividerHeight( 2 );
    // mList.setSmoothScrollbarEnabled( true );
    // mList.setFastScrollAlwaysVisible( true ); // REQUIRES API-11
    // mList.setFastScrollEnabled( true );

    // restoreInstanceFromData();
    // mLastExtend = mApp_mData.getLastShotId( TDInstance.sid );
    // List< DBlock > list = mApp_mData.selectAllShots( TDInstance.sid, TDStatus.NORMAL );

    mMenuImage = (Button) findViewById( R.id.handle );
    mMenuImage.setOnClickListener( this );
    TDandroid.setButtonBackground( mMenuImage, MyButton.getButtonBackground( this, res, R.drawable.iz_menu ) );

    mMenu = (ListView) findViewById( R.id.menu );
    mMenu.setOnItemClickListener( this );
    // setMenuAdapter( getResources() );
    // onMenu = true; // force close menu
    // closeMenu();

    // CutNPaste.dismissPopupBT();

    if ( mDataDownloader != null ) {
      mApp.registerLister( this );
    }

    // mSearch = new SearchResult();
  }

  @Override
  public void onStart() 
  {
    super.onStart();
    // Debug.startMethodTracing( "distox" );
    // TDLog.v( "Shot Activity on Start() " );
    TDLocale.resetTheLocale();
    setMenuAdapter( getResources() );
    onMenu = true; // force close menu
    closeMenu();
  }

  @Override
  public synchronized void onDestroy() 
  {
    super.onDestroy();
    // TDLog.v("ShotWindow onDestroy()" );
    // new DataStopTask( mApp, this, mDataDownloader ).execute();

    if ( doubleBackHandler != null ) {
      doubleBackHandler.removeCallbacks( doubleBackRunnable );
    }
  }

  @Override
  public synchronized void onStop() 
  {
    // Debug.stopMethodTracing( );
    super.onStop();
    // TDLog.v("ShotWindow onStop()" );
    // // if ( TDSetting.mDataBackup && TDSetting.mExportShotsFormat >= 0 ) TopoDroidApp.doExportDataAsync( TDInstance.context, TDSetting.mExportShotsFormat, false );
  }

  @Override
  public synchronized void onPause() 
  {
    super.onPause();
    // TDLog.v("ShotWindow onPause()" );
    // saveInstanceToData();

    // mApp.unregisterConnListener( mHandler );
    // if ( mApp.mComm != null ) { mApp.mComm.suspend(); }
    // FIXME NOTIFY unregister ILister
  }

  @Override
  public synchronized void onResume() 
  {
    super.onResume();
    // TDLog.v("ShotWindow onResume()" );
    // FIXME NOTIFY register ILister
    // if ( mApp.mComm != null ) { mApp.mComm.resume(); }
    // TDLog.v( "Shot Activity on Resume()" );
    
    restoreInstanceFromData();
    updateDisplay( );
    TDFeedback.reset();

    if ( mDataDownloader != null ) {
      mDataDownloader.onResume();
      // mApp.registerConnListener( mHandler );
      setConnectionStatus( mDataDownloader.getStatus() );
    }
    setRefAzimuthButton( );
  }

  // --------------------------------------------------------------

  private boolean doubleBack = false;
  private Handler doubleBackHandler = new Handler();
  private Toast   doubleBackToast = null;

  private final Runnable doubleBackRunnable = new Runnable() {
    @Override 
    public void run() {
      doubleBack = false;
      if ( doubleBackToast != null ) doubleBackToast.cancel();
      doubleBackToast = null;
    }
  };

  void doFinish()
  {
    new DataStopTask( mApp, this, mDataDownloader ).execute();
    TopoDroidApp.mShotWindow = null;
    finish();
  }

  @Override
  public void onBackPressed () // askClose
  {
    if ( closeMenu() ) return;
    if ( CutNPaste.dismissPopupBT() ) return;
    if ( onMultiselect ) {
      clearMultiSelect();
      return;
    }

    if ( doubleBack ) {
      if ( doubleBackToast != null ) doubleBackToast.cancel();
      doubleBackToast = null;
      DrawingSurface.clearManagersCache();

      new DataStopTask( mApp, this, mDataDownloader ).execute();
      // if ( TDSetting.mDataBackup ) TopoDroidApp.doExportDataAsync( getApplicationContext(), TDSetting.mExportShotsFormat, false ); // try_save
      TopoDroidApp.mShotWindow = null;
      super.onBackPressed();
      return;
    }
    doubleBack = true;
    doubleBackToast = TDToast.makeToast( R.string.double_back );
    doubleBackHandler.postDelayed( doubleBackRunnable, 1000 );
  }

  // --------------------------------------------------------------

  // FIXME NOTIFY: the display mode is local - do not notify
  private void restoreInstanceFromData()
  { 
    // TDLog.v("restore from data");
    if ( mApp_mData == null ) return;
    String shots = mApp_mData.getValue( "DISTOX_SHOTS" );
    if ( shots != null ) {
      String[] vals = shots.split( " " );
      mFlagSplay  = ( vals.length > 0 ) && vals[0].equals( TDString.ONE );
      mFlagLeg    = ( vals.length > 1 ) && vals[1].equals( TDString.ONE );
      mFlagBlank  = ( vals.length > 2 ) && vals[2].equals( TDString.ONE );
      mDataAdapter.show_ids = ( vals.length > 3 ) && vals[3].equals( TDString.ONE );
      mFlagLatest = ( vals.length > 4 ) && vals[4].equals( TDString.ONE );
    } else {
      TDLog.Error("no saved data");
    }
  }
    
  private void saveInstanceToData()
  {
    // TODO run in a Thread - this is executed when the displaymode is changed ... no need for a thread
    // TDLog.v("save to data");
    (new Thread() {
      public void run() {
        mApp_mData.setValue( "DISTOX_SHOTS",
          String.format(Locale.US, "%d %d %d %d %d", mFlagSplay?1:0, mFlagLeg?1:0, mFlagBlank?1:0, isShowIds()?1:0, mFlagLatest?1:0 ) );
      }
    } ).start();
    // TDLog.v( "save to data mFlagSplay " + mFlagSplay );
  }

  // --------------------------------------------------------------
  private void doBluetooth( Button b ) // BLUETOOTH
  {
    mApp.doBluetoothButton( mActivity, this, b, mDataAdapter.getCount() );
  }

  @Override 
  public boolean onLongClick( View view )
  {
    // TDLog.v( "on longClick " + view.getId() );
    if ( closeMenu() ) return true;
    if ( CutNPaste.dismissPopupBT() ) return true;

    boolean ret = false;
    Button b = (Button)view;
    if ( isButton1( b, BTN_SEARCH ) ) { // next search pos
      int pos = mDataAdapter.nextSearchPosition();
      // TDLog.v( "next search pos " + pos );
      jumpToPos( pos );
      // if ( mSearch != null ) jumpToPos( mSearch.nextPos() );
      // jumpToPos( mDataAdapter.nextSearchPosition() );
      ret = true;
    } else {
      mDataAdapter.clearSearch();
      if ( ! TDInstance.isDivingMode() && b == mButton1[ BTN_DOWNLOAD ] ) { // MULTI-DEVICE or SECOND-DEVICE
        if ( TDInstance.isDeviceDistoX() ) {
          if ( ! mDataDownloader.isDownloading() && TDSetting.isConnectionModeMulti() && TopoDroidApp.mDData.getDevices().size() > 1 ) {
            if ( TDSetting.mSecondDistoX && TDInstance.getDeviceB() != null ) {
              mApp.switchSecondDevice();
              setTheTitle();
              // TDToast.make( String.format( getResources().getString(R.string.using), TDInstance.deviceNickname() ) );
            } else {
              (new DeviceSelectDialog( this, mApp, mDataDownloader, this )).show();
            }
          } else {
            mDataDownloader.toggleDownload();
            mDataDownloader.doDataDownload( DataType.DATA_SHOT );
          }
        } else { // TODO something cleverer than falling back to short click
          mDataDownloader.toggleDownload();
          mDataDownloader.doDataDownload( DataType.DATA_SHOT );
        }
        ret = true;
      } else if ( isButton1( b, BTN_PLOT ) ) {
        if ( TDInstance.recentPlot != null ) {
          startExistingPlot( TDInstance.recentPlot, TDInstance.recentPlotType, null );
        } else {
          // onClick( view ); // fall back to onClick
          new PlotListDialog( mActivity, this, mApp, null ).show();
        }
        ret = true;
      } else if ( isButton1( b, BTN_MANUAL ) ) {
        new SurveyCalibrationDialog( mActivity /*, mApp */ ).show();
        ret = true;
      }
    }
    return ret;
  } 

  @Override 
  public void onClick(View view)
  {
    // TDLog.v( "on click " + view.getId() );
    if ( closeMenu() ) return;
    if ( CutNPaste.dismissPopupBT() ) return;

    mDataAdapter.clearSearch();

    Button b = (Button)view;
    if ( b == mMenuImage ) {
      if ( mMenu.getVisibility() == View.VISIBLE ) {
        mMenu.setVisibility( View.GONE );
        onMenu = false;
      } else {
        mMenu.setVisibility( View.VISIBLE );
        onMenu = true;
      }
      return;
    }

    if ( b != null ) {
      Intent intent;

      int k1 = 0;
      int kf = 0;
      // int k2 = 0;
      if ( ! TDInstance.isDivingMode() ) {
        if ( k1 < mNrButton1 && b == mButton1[k1++] ) {        // DOWNLOAD
          if ( TDInstance.getDeviceA() != null ) {
            // mSearch = null; // invalidate search
            // if ( mBTstatus == ConnectionState.CONN_DISCONNECTED ) {
            //   TDToast.make( R.string.connecting );
            // }
            // TDLog.Log( TDLog.LOG_INPUT, "Download button, mode " + TDSetting.mConnectionMode );
            
            // toggle must come first in the test
            boolean downloading = mDataDownloader.toggleDownload();
            TDFeedback.notifyFeedback( this, downloading && mBTstatus == ConnectionState.CONN_DISCONNECTED );
            mApp.notifyStatus( downloading ? ConnectionState.CONN_WAITING : ConnectionState.CONN_DISCONNECTED );
            // TDLog.v( "Download, conn mode " + TDSetting.mConnectionMode + " download status " + mDataDownloader.getStatus() );
            // setConnectionStatus( mDataDownloader.getStatus() );
            mDataDownloader.doDataDownload( DataType.DATA_SHOT );
          } else {
            TDLog.Error("Shot Window: null device A");
          }
	  return;
        } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // BT RESET
          doBluetooth( b );
	  return;
        }
      }
      if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // DISPLAY 
        // mSearch = null; // invalidate search
        new ShotDisplayDialog( mActivity, this ).show();
      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // PLOT LIST
        new PlotListDialog( mActivity, this, mApp, null ).show();
      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // NOTE
        if ( TDInstance.survey != null ) {
          (new DialogAnnotations( mActivity, TDInstance.survey )).show();
        }

      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // ADD MANUAL SHOT
        if ( TDLevel.overBasic ) {
          // mSearch = null; // invalidate search
          DBlock last_blk = mApp_mData.selectLastShotWithFromStation( TDInstance.sid );
          (new ShotNewDialog( mActivity, mApp, this, last_blk, -1L )).show();
        }
      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // AZIMUTH
        if ( TDLevel.overNormal ) {
          if ( TDSetting.mAzimuthManual ) {
            setRefAzimuth( TDAzimuth.mRefAzimuth, - TDAzimuth.mFixedExtend );
          } else {
            (new AzimuthDialog( mActivity, this, TDAzimuth.mRefAzimuth, mBMdial )).show();
            // FIXME_AZIMUTH_DIAL (new AzimuthDialog( mActivity, this, TDAzimuth.mRefAzimuth, mDialBitmap )).show();
          }
        }

      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // SAVED STATIONS
        if ( TDLevel.overAdvanced ) {
          (new CurrentStationDialog( mActivity, this, mApp, mApp.getCurrentOrLastStation() )).show();
          // ArrayList< DBlock > list = numberSplays(); // SPLAYS splays numbering no longer active
          // if ( list != null && list.size() > 0 ) {
          //   updateDisplay( );
          // }
        }
      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // SEARCH
        if ( TDLevel.overAdvanced ) {
          new SearchDialog( mActivity, this, mDataAdapter.getSearchName() ).show();
        }
      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // REFRESH
        if ( TDLevel.overExpert ) {
          updateDisplay();
	}

      } else if ( kf < mNrButtonF && b == mButtonF[kf++] ) { // LEFT reset stretch
        for ( DBlock blk : mDataAdapter.mSelect ) {
          blk.setExtend( ExtendType.EXTEND_LEFT, ExtendType.STRETCH_NONE );
          mApp_mData.updateShotExtend( blk.mId, TDInstance.sid, ExtendType.EXTEND_LEFT, ExtendType.STRETCH_NONE );
        }
        clearMultiSelect( );
        updateDisplay();
      } else if ( kf < mNrButtonF && b == mButtonF[kf++] ) { // FLIP
        for ( DBlock blk : mDataAdapter.mSelect ) {
          if ( blk.flipExtendAndStretch() ) {
            mApp_mData.updateShotExtend( blk.mId, TDInstance.sid, blk.getIntExtend(), blk.getStretch() );
          }
        }
        clearMultiSelect( );
        updateDisplay();
      } else if ( kf < mNrButtonF && b == mButtonF[kf++] ) { // RIGHT reset stretch
        for ( DBlock blk : mDataAdapter.mSelect ) {
          blk.setExtend( ExtendType.EXTEND_RIGHT, ExtendType.STRETCH_NONE );
          mApp_mData.updateShotExtend( blk.mId, TDInstance.sid, ExtendType.EXTEND_RIGHT, ExtendType.STRETCH_NONE );
        }
        clearMultiSelect( );
        updateDisplay();
        // mList.invalidate(); // NOTE not enough to see the change in the list immediately
      } else if ( TDLevel.overExpert && kf < mNrButtonF && b == mButtonF[kf++] ) { // HIGHLIGHT
        // ( blks == null || blks.size() == 0 ) cannot happen
        (new MultishotDialog( mActivity, this, mDataAdapter.mSelect )).show();
      //   highlightBlocks( mDataAdapter.mSelect );
      //   // clearMultiSelect( );
      //   // updateDisplay();
      // } else if ( kf < mNrButtonF && b == mButtonF[kf++] ) { // RENUMBER SELECTED SHOTS
      //   renumberBlocks( mDataAdapter.mSelect );
      //   clearMultiSelect( );
      //   mList.invalidate();
      // } else if ( kf < mNrButtonF && b == mButtonF[kf++] ) { // BEDDING
      //   computeBedding( mDataAdapter.mSelect );
      } else if ( kf < mNrButtonF && b == mButtonF[kf++] ) { // DELETE - CUT
        askMultiDelete();
      } else if ( kf < mNrButtonF && b == mButtonF[kf++] ) { // COPY
        doMultiCopy();
      } else if ( kf < mNrButtonF && b == mButtonF[kf++] ) { // CANCEL
        clearMultiSelect( );
        mList.invalidate();
      }
    }
  }

  // ----------------------------------------------------------------
  // SEARCH

  void searchStation( String name, boolean splays )
  {
    // if ( mSearch != null ) {
      // mSearch.set( name, mDataAdapter.searchStation( name, splays ) );
      mDataAdapter.searchStation( name, splays );
      // if ( ! jumpToPos( mSearch.nextPos() ) ) 
      if ( ! jumpToPos( mDataAdapter.nextSearchPosition() ) ) {
        TDToast.make( R.string.station_not_found );
      }
    // }
  }

  void searchShot( long flag ) 
  {
    // if ( mSearch != null ) {
      // mSearch.set( null, mDataAdapter.searchShot( flag ) );
      mDataAdapter.searchShot( flag );
      // if ( ! jumpToPos( mSearch.nextPos() ) ) 
      if ( ! jumpToPos( mDataAdapter.nextSearchPosition() ) ) {
        TDToast.make( R.string.shot_not_found );
      }
    // }
  }

  boolean jumpToPos( final int pos ) 
  {
    if ( pos < 0 ) return false;
    mList.post( new Runnable() {
      @Override
      public void run() {
        mList.setSelection( pos );
        View v = mList.getChildAt( pos );
        if ( v != null ) v.requestFocus();
      }
    } );
    return true;
  }
   
  // ----------------------------------------------------------------
  /** ask whether to cut (delete) a set of shots
   */
  private void askMultiDelete()
  {
    Resources res = getResources();
    TopoDroidAlertDialog.makeAlert( mActivity, res, res.getString(R.string.shots_delete),
      res.getString(R.string.button_ok), 
      res.getString(R.string.button_cancel),
      new DialogInterface.OnClickListener() { // ok handler
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          doMultiDelete();
        } },
      new DialogInterface.OnClickListener() { // cancel handler
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          clearMultiSelect( );
          mList.invalidate();
        } }
    );
  }

  /** cut (delete) a set of shots and put them in the buffer
   */
  void doMultiDelete()
  {
    checkXSections( mDataAdapter.mSelect );

    if ( TDLevel.overAdvanced ) mDBlockBuffer.clear();
    for ( DBlock blk : mDataAdapter.mSelect ) {
      if ( TDLevel.overAdvanced ) mDBlockBuffer.add( blk );
      long id = blk.mId;
      mApp_mData.deleteShot( id, TDInstance.sid, TDStatus.DELETED );
      // mSurveyAccuracy.removeBlockAMD( blk ); // not necessary: done by updateDisplay
      if ( /* blk != null && */ blk.isMainLeg() ) { // == DBlock.BLOCK_MAIN_LEG 
        if ( mFlagLeg ) {
          for ( ++id; ; ++id ) {
            DBlock b = mApp_mData.selectShot( id, TDInstance.sid );
            if ( b == null || ! b.isSecLeg() ) { // != DBlock.BLOCK_SEC_LEG
              break;
	    }
            mApp_mData.deleteShot( id, TDInstance.sid, TDStatus.DELETED );
            // mSurveyAccuracy.removeBlockAMD( b );
          }
        } else { // set station to next leg shot
          ++id;
          DBlock b = mApp_mData.selectShot( id, TDInstance.sid );
          if ( b != null && b.isSecLeg() ) { // DBlock.BLOCK_SEC_LEG --> leg-flag 0
            mApp_mData.updateShotNameAndDataStatus( id, TDInstance.sid, blk.mFrom, blk.mTo, blk.getIntExtend(), blk.getFlag(), 0, blk.mComment, 0 );
            // mApp_mData.updateShotStatus( id, TDInstance.sid, 0 ); // status normal
          }
        }
      }
    }
    clearMultiSelect( );
    updateDisplay( ); 
  }

  /** copy a set of shots to the buffer
   */
  void doMultiCopy()
  {
    if ( TDLevel.overAdvanced ) mDBlockBuffer.clear();
    for ( DBlock blk : mDataAdapter.mSelect ) {
      if ( TDLevel.overAdvanced ) mDBlockBuffer.add( blk );
      if ( /* blk != null && */ blk.isMainLeg() ) { // == DBlock.BLOCK_MAIN_LEG 
        if ( mFlagLeg ) {
          for ( long id = blk.mId+1; ; ++id ) {
            DBlock b = mApp_mData.selectShot( id, TDInstance.sid );
            if ( b == null || ! b.isSecLeg() ) { // != DBlock.BLOCK_SEC_LEG
              break;
	    }
            if ( TDLevel.overAdvanced ) mDBlockBuffer.add( b );
          }
        }
      }
    }
    clearMultiSelect( );
    updateDisplay( ); 
  }

  // ------------------------------------------------------------------

  public boolean hasSurveyStation( String start )
  {
    return mApp_mData.hasSurveyStation( TDInstance.sid, start );
  }

  // ---------------------------------------------------------------------------------

  public DBlock getNextBlankLegShot( DBlock blk )
  {
    DBlock ret = null;
    long id = 0;
    for ( int k=0; k<mDataAdapter.getCount(); ++k ) {
      DBlock b = mDataAdapter.get(k);
      if ( b == null ) return null;
      if ( b.isTypeBlank() ) {
        id = b.mId - 1;
        break;
      }
    }
    List< DBlock > list = mApp_mData.selectShotsAfterId( TDInstance.sid, id , 0 );
    for ( DBlock b : list ) {
      if ( b.isTypeBlank() ) {
        // TDLog.v( "BLANK " + b.mLength + " " + b.mBearing + " " + b.mClino );
        if ( ret != null && ret.isRelativeDistance( b ) ) return ret;
        ret = b;
      } else if ( b.isSecLeg() ) {
        // TDLog.v( "LEG " + b.mLength + " " + b.mBearing + " " + b.mClino );
        if ( ret != null &&  ret.isRelativeDistance( b ) ) return ret;
      } else {
        // TDLog.v( "OTHER " + b.mLength + " " + b.mBearing + " " + b.mClino );
        ret = null;
      }
    }
    return null;
  }

  // get the next centerline shot and set mNextPos index
  public DBlock getNextLegShot( DBlock blk, boolean move_down )
  {
    // TDLog.Log( TDLog.LOG_SHOT, "getNextLegShot: pos " + mShotPos );
    if ( blk == null ) {
      // TDLog.Log( TDLog.LOG_SHOT, "   block is null");
      return null;
    }
    if ( move_down ) {
      mPrevPos = mShotPos;
      mShotPos = mNextPos;
      mNextPos = mPrevPos; // the old mShotPos;
    } else {
      mNextPos = mShotPos;
    }
	// OLD CODE
	// while ( mNextPos < mDataAdapter.getCount() && blk != mDataAdapter.get(mNextPos) ) ++ mNextPos;
    // ++ mNextPos; // one position after blk
    // while ( mNextPos < mDataAdapter.getCount() ) {
    //   DBlock b = mDataAdapter.get(mNextPos);
    //   int t = b.getBlockType();
    //   if ( t == DBlock.BLOCK_MAIN_LEG ) {
    //     return b;
    //   } else if (    DBlock.isTypeBlank( t )
    //               && mNextPos+1 < mDataAdapter.getCount()
    //               && b.isRelativeDistance( mDataAdapter.get(mNextPos+1) ) ) {
    //     return b;
    //   }
    //   ++ mNextPos;
    // }
    // NEW CODE
    for ( ; mNextPos < mDataAdapter.getCount(); ++ mNextPos ) {
      DBlock b = mDataAdapter.get(mNextPos);
      if ( b == null ) return null;
      if ( b == blk ) break;
    }
    // start at one position after blk
    for ( ++mNextPos; mNextPos < mDataAdapter.getCount(); ++mNextPos ) {
      DBlock b = mDataAdapter.get(mNextPos);
      if ( b == null ) return null;
      // int t = b.getBlockType();
      if ( b.isMainLeg() ) { // t == DBlock.BLOCK_MAIN_LEG 
        return b;
      } else if (    b.isTypeBlank( )
                  && mNextPos+1 < mDataAdapter.getCount()
                  && b.isRelativeDistance( mDataAdapter.get(mNextPos+1) ) ) {
        return b;
      }
    }
    return null;
  }

  // get the previous centerline shot and set the mPrevPos index
  public DBlock getPreviousLegShot( DBlock blk, boolean move_up )
  {
    // TDLog.Log( TDLog.LOG_SHOT, "getPreviousLegShot: pos " + mShotPos );
    if ( blk == null ) return null;
    if ( move_up ) {
      mNextPos = mShotPos;
      mShotPos = mPrevPos;
      mPrevPos = mNextPos; // the old mShotPos;
    } else {
      mPrevPos = mShotPos;
    }
	// OLD CODE
	// while ( mPrevPos >= 0 && blk != mDataAdapter.get(mPrevPos) ) -- mPrevPos;
    // while ( mPrevPos > 0 ) {
    //   -- mPrevPos;
    //   DBlock b = mDataAdapter.get(mPrevPos);
    //   if ( b.isMainLeg() ) {
    //     return b;
    //   }
    // }
    // NEW CODE
    for ( ; mPrevPos >= 0; -- mPrevPos ) {
      DBlock b = mDataAdapter.get(mPrevPos);
      if ( b == null ) return null;
      if ( b == blk ) break;
    }
    for ( --mPrevPos; mPrevPos >= 0; --mPrevPos ) {
      DBlock b = mDataAdapter.get(mPrevPos);
      if ( b == null || b.isMainLeg() ) {
        return b;
      }
    }
    /* if ( mPrevPos < 0 ) */ mPrevPos = 0;
    return null;
  }

  void updateShotDistanceBearingClino( float d, float b, float c, DBlock blk )
  {
    // TDLog.v( "update shot DBC length " + d );
    mApp_mData.updateShotDistanceBearingClino( blk.mId, TDInstance.sid, d, b, c );
    checkSiblings( blk, blk.mFrom, blk.mTo, d, b, c );
    blk.mLength  = d;
    blk.mBearing = b;
    blk.mClino   = c;
    mDataAdapter.updateBlockView( blk.mId );
  }

  void updateShotDepthBearingDistance( float p, float b, float d, DBlock blk )
  {
    // TDLog.v( "update shot DBC length " + d );
    mApp_mData.updateShotDepthBearingDistance( blk.mId, TDInstance.sid, p, b, d );
    checkSiblings( blk, blk.mFrom, blk.mTo, d, b, p );
    blk.mLength  = d;
    blk.mBearing = b;
    blk.mDepth   = p;
    mDataAdapter.updateBlockView( blk.mId );
  }

  /** update the shot stations and the flags
   * @param from    FROM station
   * @param to      TO station
   * @param extend  integer extend
   * @param stretch fractional extend
   * @param flag    shot flags
   * @param leg     leg data-helper value (0 normal, 1 sec, 2 x-splay, 3 back, 4 h-splay, 5 v-splay
   * @param comment shot comment
   * @param blk     shot data block
   * @note called only by ShotDialog 
   */
  void updateShotNameAndFlags( String from, String to, int extend, float stretch, long flag, long leg, String comment, DBlock blk )
  {
    // TDLog.Log( TDLog.LOG_SHOT, "update Shot From >" + from + "< To >" + to + "< comment " + comment );
    // TDLog.v("update shot " + from + "-" + to + " leg " + leg + "/" + blk.getLegType()
    //       + " blk type " + blk.getBlockType() + " comment " + comment );

    checkXSections( blk );
    blk.setBlockName( from, to, (leg == LegType.BACK) );

    int ret = mApp_mData.updateShotNameAndData( blk.mId, TDInstance.sid, from, to, extend, flag, leg, comment );

    if ( ret == -1 ) {
      TDToast.makeBad( R.string.no_db );
    // } else if ( ret == -2 ) {
    //   TDToast.make( R.string.makes_cycle );
    } else {
      // update same shots of the given block
      List< DBlock > blk_list = mApp_mData.selectShotsAfterId( blk.mId, TDInstance.sid, 0L );
      for ( DBlock blk1 : blk_list ) {
        if ( ! blk1.isRelativeDistance( blk ) ) break;
        mApp_mData.updateShotLeg( blk1.mId, TDInstance.sid, LegType.EXTRA );
      }
    }

    if ( leg == LegType.EXTRA ) {
      blk.setTypeSecLeg();
    } else if ( leg == LegType.BACK ) {
      blk.setTypeBackLeg();
    // } else if ( blk.isBackLeg() ) {
    //   blk.setTypeMainLeg();
    }

    // TDLog.v( "update block id " + blk.mId );
    DBlock blk3 = mDataAdapter.updateBlockView( blk.mId );
    if ( blk3 != blk && blk3 != null ) {
      blk3.setBlockName( from, to, (leg == LegType.BACK) );
      blk3.setExtend( extend, stretch );
      blk3.resetFlag( flag );
      blk3.mComment = comment;
      // FIXME if ( leg == LegType.EXTRA ) blk3.setBlockType( DBlock.BLOCK_SEC_LEG );
      mDataAdapter.updateBlockView( blk3.mId );
    }
  }

  /* FIXME_HIGHLIGHT
  // open the sketch and highlight block in the sketch
  // called by MultishotDialog
  void highlightBlocks( List< DBlock > blks )  // HIGHLIGHT
  {
    mApp.setHighlighted( blks );
    // TDLog.v( "highlight blocks [0] " + ( (blks==null)? "null" : blks.size() ) );
    if ( blks == null || blks.size() == 0 ) return;
    // now if there is a plot open it
    if ( TDInstance.recentPlot != null ) {
      startExistingPlot( TDInstance.recentPlot, TDInstance.recentPlotType, blks.get(0).mFrom );
    }
    clearMultiSelect( );
  }
  */

  // open the sketch and highlight block in the sketch
  // called by MultishotDialog
  void colorBlocks( List< DBlock > blks, int color )  // HIGHLIGHT
  {
    // TDLog.v( "highlight blocks [0] " + ( (blks==null)? "null" : blks.size() ) );
    if ( blks == null || blks.size() == 0 ) return;
    for ( DBlock blk : blks ) {
      blk.clearPaint();
      // mApp_mData.updateShotColor( blk.mId, TDInstance.sid, color );
    }
    mApp_mData.updateShotsColor( blks, TDInstance.sid, color );
    // if ( TDInstance.recentPlot != null ) {
    //   startExistingPlot( TDInstance.recentPlot, TDInstance.recentPlotType, blks.get(0).mFrom );
    // }
    clearMultiSelect( );
    // ... and refresh list
    updateDisplay();
  }

  /** update the FROM-TO stations of a shot database record
   * @param bid    block ID
   * @param from   FROM station
   * @param to     TO station
   */
  private void updateShotName( long bid, String from, String to )
  {
    mApp_mData.updateShotName( bid, TDInstance.sid, from, to );
  }

  /** update the FROM-TO stations of a shot database record
   * @param blk    data block 
   * @param from   FROM station
   * @param to     TO station
   * @note used by the DBlockAdapter to react to edit actions
   */
  void updateDBlockName( DBlock blk, String from, String to )
  {
    checkXSections( blk );
    updateShotName( blk.mId, from, to );
    blk.setBlockName( from, to, blk.isBackLeg() ); 
  }
    // TODO check if the blk stations have xsections linked to

  /** check the sibling shots and toast a warning if there is a bad sibling
   * @param blk    data block 
   * @param from   FROM station
   * @param to     TO station
   * @param d      distance
   * @param b      azimuth [deg.]
   * @param c      clino [deg.]
   */
  private void checkSiblings( DBlock blk,  String from, String to, float d, float b, float c )
  {
    if ( ! blk.isLeg() ) return;
    if ( mApp_mData.checkSiblings( blk.mId, TDInstance.sid, from, to, d, b, c ) ) { // bad sibling
      // TDLog.v("shot window detect bad sibling");
      TDToast.makeWarn( R.string.bad_sibling );
    }
  }

  private boolean checkXSections( DBlock blk )
  {
    if ( ! blk.isLeg() ) return false;
    ArrayList<String> names = mApp_mData.getXSectionStations( TDInstance.sid );
    // for ( String name : names ) TDLog.v("check xsection [1] station: " + name );
    if (  ( blk.mFrom != null && blk.mFrom.length() > 0 && names.contains( blk.mFrom ) ) 
       || ( blk.mTo != null && blk.mTo.length() > 0 && names.contains( blk.mTo ) ) ) {
      TDToast.makeWarn( R.string.warning_station_xsection );
      return true;
    }
    return false;
  }

  private boolean checkXSections( List< DBlock > blks )
  {
    if ( blks.size() == 0 ) return false;
    ArrayList<String> names = mApp_mData.getXSectionStations( TDInstance.sid );
    // for ( String name : names ) TDLog.v("check xsection [2] station: " + name );
    for ( DBlock b : blks ) {
      if ( b.isLeg() ) { 
        if (  ( b.mFrom != null && b.mFrom.length() > 0 && names.contains( b.mFrom ) ) 
           || ( b.mTo != null && b.mTo.length() > 0 && names.contains( b.mTo ) ) ) {
          TDToast.makeWarn( R.string.warning_station_xsection );
          return true;
        }
      }
    }
    return false;
  }

  /** rename a set of data blocks
   * @param blks    list of blocks to renumber
   * @param from    FROM station to assign to first block
   * @param to      TO station to assign to first block
   * @note only called by MultishotDialog
   * no need to synchronize
   */
  void renumberBlocks( List< DBlock > blks, String from, String to )  // RENUMBER SELECTED BLOCKS
  {
    checkXSections( blks );

    if ( from.length() == 0 && to.length() == 0 ) {
      for ( DBlock b : blks ) {
        b.setBlockName( from, to );
        updateShotName( b.mId, from, to );
      }
      updateDisplay();
      return;
    }

    DBlock blk = blks.get(0); // blk is guaranteed to exists
    if ( ! ( from.equals(blk.mFrom) && to.equals(blk.mTo) ) ) {
      blk.setBlockName( from, to, blk.isBackLeg() );
      updateShotName( blk.mId, from, to );
    }
    if ( blk.isLeg() ) {
      mApp.assignStationsAfter( blk, blks /*, stations */ );
      updateDisplay();
      // mList.invalidate();
      checkSiblings( blk, from, to, blk.mLength, blk.mBearing, blk.mClino );
    } else if ( blk.isSplay() ) { // FIXME RENUMBER ONLY SPLAYS
      for ( DBlock b : blks ) {
        if ( b == blk ) continue;
        b.setBlockName( from, to );
        updateShotName( b.mId, from, to );
      }
      updateDisplay();
    } else {
      TDToast.makeBad( R.string.no_leg_first );
    }
    clearMultiSelect( );
  }

  void swapBlocksName( List< DBlock > blks )  // SWAP SELECTED BLOCKS STATIONS
  {
    checkXSections( blks );

    // TDLog.v( "swap list size " + blks.size() );
    for ( DBlock blk : blks ) {
      String from = blk.mTo;
      String to   = blk.mFrom;
      // TDLog.v( "swap block to <" + from + "-" + to + ">" );
      blk.setBlockName( from, to );
      // updateShotName( blk.mId, from, to );
      checkSiblings( blk, from, to, blk.mLength, blk.mBearing, blk.mClino );
    }
    mApp_mData.updateShotsName( blks, TDInstance.sid );
    updateDisplay();
  }

  /** bedding: solve Sum (A*x + B*y + C*z + 1)^2 minimum
   *   xx  xy  xz   A   -xn
   *   xy  yy  yz * B = -yn
   *   xz  yz  zz   C   -zn
   *
   *   det = xx * (yy * zz - yz^2) - xy * (xy * zz - xz * yz) + xz * (xy * yz - yy * xz) 
   *       = xx yy zz - xx yz^2 - yy xz^2 - zz xy^2 + 2 xy xz yz
   *  the DIP is downwards and 90 deg. right from the STRIKE, thus
   *    D x S = N
   *    S x N = D
   *    S = ( Z x N ) normalized
   *
   *          ^ Z
   *          |  Normal
   *          | /
   *          |/______ Strike
   *           \
   *            \ Dip
   *  The dip angle is measured from the horizontal plane
   *  The strike from the north
   */
  String computeBedding( List< DBlock > blks ) // BEDDING
  {
    String strike_dip = getResources().getString(R.string.few_data);
    if ( blks != null && blks.size() > 2 ) {
      DBlock b0 = blks.get(0);
      String st = b0.mFrom;
      float xx = 0, xy = 0, yy = 0, xz = 0, zz = 0, yz = 0, xn = 0, yn = 0, zn = 0;
      int nn = 0;
      if ( st != null && st.length() > 0 ) {
        for ( DBlock b : blks ) {
          if ( st.equals( b.mFrom ) ) {
            float h = TDMath.cosd(b.mClino);
            float z = TDMath.sind(b.mClino);
            float y = h * TDMath.cosd(b.mBearing);
            float x = h * TDMath.sind(b.mBearing);
            xx += x * x;
            xy += x * y;
            xz += x * z;
            yy += y * y;
            yz += y * z;
            zz += z * z;
            xn += x;
            yn += y;
            zn += z;
            ++ nn;
          }
        }
      } else {
        st = b0.mTo;
        if ( st != null && st.length() > 0 ) {
          for ( DBlock b : blks ) {
            if ( st.equals( b.mTo ) ) {
              float h = TDMath.cosd(b.mClino);
              float z = TDMath.sind(b.mClino);       // vertical upwards
              float y = h * TDMath.cosd(b.mBearing); // north
              float x = h * TDMath.sind(b.mBearing); // east
              xx += x * x;
              xy += x * y;
              xz += x * z;
              yy += y * y;
              yz += y * z;
              zz += z * z;
              xn += x;
              yn += y;
              zn += z;
	      ++ nn;
            }
          }
	}
      }
      if ( nn >= 3 ) {
	String strike_fmt = getResources().getString( R.string.strike_dip );
	String strike_regex = strike_fmt.replaceAll("%\\d\\$.0f", "\\-??\\\\d+"); 
	// TDLog.v( "Strike regex: <<" + strike_regex + ">>");
        TDMatrix m = new TDMatrix( new TDVector(xx, xy, xz), new TDVector(xy, yy, yz), new TDVector(xz, yz, zz) );
        TDMatrix minv = m.InverseT(); // m is self-transpose
        TDVector n0 = new TDVector( -xn, -yn, -zn );
        TDVector n1 = minv.timesV( n0 ); // n1 = (a,b,c)
        if ( n1.z < 0 ) { n1.x = - n1.x; n1.y = - n1.y; n1.z = - n1.z; } // make N positive Z
        n1.normalize();
        // TDLog.v( "Plane normal " + n1.x + " " + n1.y + " " + n1.z );

        // TDVector z0 = new TDVector( 0, 0, 1 );
        // TDVector stk = z0.cross( n1 );  // strike = ( -n1.y, n1.x, 0 );
        // stk.normalized();
        // TDVector dip = n1.cross( stk ); // dip
        // float astk = TDMath.atan2d( stk.x, stk.y ); // TDMath.atan2d( -n1.y, n1.x );
        // float adip = TDMath.acosd( dip.z ); // TDMath.asind( n1.z );
        float astk = TDMath.atan2d( -n1.y, n1.x );
        float adip = 90 - TDMath.asind( n1.z );
        strike_dip = String.format( strike_fmt, astk, adip );
        // TDToast.make( strike_dip );
        if ( b0.mComment != null && b0.mComment.length() > 0 ) {
	  if ( b0.mComment.matches( ".*" + strike_regex + ".*" ) ) {
	    // TDLog.v( "Strike regex is contained");
            b0.mComment = b0.mComment.replaceAll( strike_regex, strike_dip );
	  } else {
            b0.mComment = b0.mComment + " " + strike_dip;
          }
  	} else {
          b0.mComment = strike_dip;
	}
	// TDLog.v( "Comment <<" + b0.mComment + ">>");
	mApp_mData.updateShotComment( b0.mId, TDInstance.sid, b0.mComment );
	b0.invalidate();
      }
    }
    // clearMultiSelect( );
    // mList.invalidate();
    return strike_dip;
  }
  
  // NOTE called only by ShotDialog.saveDBlock() with to.length() == 0 ie to == "" and blk splay shot
  // update stations for all splay blocks with sme from as this block
  // @param extend   this block new extend
  // @param flag     this block new flag
  // ...
  // @param blk      this block
  //
  public void updateSplayShots( String from, String to, long extend, long flag, long leg, String comment, DBlock blk )
  {
    ArrayList< DBlock > splays = mDataAdapter.getSplaysAtId( blk.mId, blk.mFrom );
    for ( DBlock b : splays ) {
      if ( b.mId == blk.mId ) {
        blk.setBlockName( from, to );
        // FIXME leg should be LegType.NORMAL
        int ret = mApp_mData.updateShotNameAndData( blk.mId, TDInstance.sid, from, to, extend, flag, leg, comment );

        if ( ret == -1 ) {
          TDToast.makeBad( R.string.no_db );
        // } else if ( ret == -2 ) {
        //   TDToast.make( R.string.makes_cycle );
        // } else {
          // // update same shots of the given block: SHOULD NOT HAPPEN
          // List< DBlock > blk_list = mApp_mData.selectShotsAfterId( blk.mId, TDInstance.sid, 0L );
          // for ( DBlock blk1 : blk_list ) {
          //   if ( ! blk1.isRelativeDistance( blk ) ) break;
          //   mApp_mData.updateShotLeg( blk1.mId, TDInstance.sid, LegType.Extra );
          // }
        }
      } else {
        b.setBlockName( from, to );
        updateShotName( b.mId, from, to ); // FIXME use
      }
      mDataAdapter.updateBlockView( b.mId );
    }
  }

  // ------------------------------------------------------------------------

  @Override
  public boolean onKeyDown( int code, KeyEvent event )
  {
    switch ( code ) {
      case KeyEvent.KEYCODE_BACK: // HARDWARE BACK (4)
        onBackPressed();
        return true;
      case KeyEvent.KEYCODE_MENU:   // HARDWRAE MENU (82)
        UserManualActivity.showHelpPage( mActivity, getResources().getString( HELP_PAGE ));
        return true;
      // case KeyEvent.KEYCODE_VOLUME_UP:   // (24)
      // case KeyEvent.KEYCODE_VOLUME_DOWN: // (25)
      default:
        // TDLog.Error( "key down: code " + code );
    }
    return false;
  }

  // ---------------------------------------------------------

  private void setMenuAdapter( Resources res )
  {
    int k = 0;
    ArrayAdapter< String > menu_adapter = new ArrayAdapter<>(mActivity, R.layout.menu );

    menu_adapter.add( res.getString( menus[k++] ) );                // menu_survey
    menu_adapter.add( res.getString( menus[k++] ) );                // menu_close
    if ( TDLevel.overBasic  ) {                                     // menu_recover
      if ( TDLevel.overAdvanced ) {
        menu_adapter.add( res.getString( menus[10] ) ); // k + 8
      } else {
        menu_adapter.add( res.getString( menus[k] ) );
      }
      k++;
    }
    if ( TDLevel.overNormal ) menu_adapter.add( res.getString( menus[k] ) ); k++; // menu_photo  
    if ( TDLevel.overExpert ) menu_adapter.add( res.getString( menus[k] ) ); k++; // menu_audio  
    if ( TDSetting.mWithSensors && TDLevel.overNormal ) menu_adapter.add( res.getString( menus[k] ) ); k++; // menu_sensor
    if ( TDLevel.overBasic ) menu_adapter.add( res.getString( menus[k] ) ); k++; // menu_3d
    if ( TDLevel.overNormal && ! TDInstance.isDivingMode() ) menu_adapter.add( res.getString( menus[k] ) ); k++; // menu_device
    menu_adapter.add( res.getString( menus[k++] ) );  // menu_options
    menu_adapter.add( res.getString( menus[k++] ) );  // menu_help
    mMenu.setAdapter( menu_adapter );
    mMenu.invalidate();
  }

  private boolean closeMenu()
  {
    if ( onMenu ) {
      mMenu.setVisibility( View.GONE );
      onMenu = false;
      return true;
    }
    return false;
  }


  void recomputeItems( String st, int pos )
  {
    if ( mFlagSplay ) {
      if ( ! mShowSplay.remove( st ) ) {
        mShowSplay.add( st );
      }
      updateDisplay( );
      mList.setSelection( (pos>5)? pos-5 : 0 );
    }
  }

  private boolean showSplaysContains( String name ) 
  {
    for ( String st : mShowSplay ) {
      if ( st.equals( name ) ) return true;
    }
    return false;
  }


  public void setConnectionStatus( int status )
  { 
    if ( TDInstance.isDivingMode() ) return;
    if ( TDInstance.getDeviceA() == null ) {
      mBTstatus = ConnectionState.CONN_DISCONNECTED;
      // mButton1[ BTN_DOWNLOAD ].setVisibility( View.GONE );
      TDandroid.setButtonBackground( mButton1[BTN_DOWNLOAD], mBMdownload_no );
      TDandroid.setButtonBackground( mButton1[BTN_BLUETOOTH], mBMbluetooth_no );
    } else {
      if ( status != mBTstatus ) {
        mBTstatus = status;
        // TDLog.v( "set button, status " + status );
        // mButton1[ BTN_DOWNLOAD ].setVisibility( View.VISIBLE );
        switch ( status ) {
          case ConnectionState.CONN_CONNECTED:
            TDFeedback.notifyFeedback( this, true );
            TDandroid.setButtonBackground( mButton1[BTN_DOWNLOAD], mBMdownload_on );
            TDandroid.setButtonBackground( mButton1[BTN_BLUETOOTH], (TDInstance.isDeviceBric() ? mBMbluetooth : mBMbluetooth_no ) );
            break;
          case ConnectionState.CONN_WAITING:
            TDandroid.setButtonBackground( mButton1[BTN_DOWNLOAD], mBMdownload_wait );
            TDandroid.setButtonBackground( mButton1[BTN_BLUETOOTH], (TDInstance.isDeviceBric() ? mBMbluetooth : mBMbluetooth_no ) );
            break;
          default:
            TDFeedback.notifyFeedback( this, false );
            TDandroid.setButtonBackground( mButton1[BTN_DOWNLOAD], mBMdownload );
            TDandroid.setButtonBackground( mButton1[BTN_BLUETOOTH], mBMbluetooth );
        }
      }
    }
  }

  public void enableBluetoothButton( boolean enable )
  {
    if ( TDInstance.isDivingMode() ) return;
    if ( TDInstance.hasBleDevice() ) enable = true;
    TDandroid.setButtonBackground( mButton1[BTN_BLUETOOTH], (enable ? mBMbluetooth : mBMbluetooth_no) );
    mButton1[BTN_BLUETOOTH].setEnabled( enable );
  }

  // void enableSketchButton( boolean enabled )
  // {
  //   mApp.mEnableZip = enabled;
  //   mButton1[ BTN_PLOT - boff ].setEnabled( enabled ); // FIXME PLOT BUTTON 
  //   TDandroid.setButtonBackground( mButton1[ BTN_PLOT - boff ], (enabled ? mBMplot : mBMplot_no) );
  // }

  // ------------------------------------------------------------------

  // only called by ShotDialog
  // @param blk   shot after which to renumber
  // no need to synchronize
  void renumberShotsAfter( DBlock blk )
  {
    // TDLog.v( "renumber shots after " + blk.mLength + " " + blk.mBearing + " " + blk.mClino );
    // NEED TO FORWARD to the APP to change the stations accordingly
 
    List< DBlock > shots;
    // backsight and tripod seem to be OK
    // if ( StationPolicy.doTripod() || StationPolicy.doBacksight() ) {
    //   shots = mApp_mData.selectAllShots( TDInstance.sid, TDStatus.NORMAL );
    // } else {
      shots = mApp_mData.selectAllShotsAfter( blk.mId, TDInstance.sid, TDStatus.NORMAL );
    // }
    // TDLog.v( "shots " + shots.size() );
    mApp.assignStationsAfter( blk, shots /*, stations */ );

    // DEBUG re-assign all the stations
    // List< DBlock > shots = mApp_mData.selectAllShots( TDInstance.sid, TDStatus.NORMAL );
    // mApp.assign Stations( shots );

    updateDisplay();
  }


  // merge this block to the following (or second following) block if this is a leg
  // if success update FROM/TO of the block
  long mergeToNextLeg( DBlock blk )
  {
    long id = mApp_mData.mergeToNextLeg( blk, TDInstance.sid );
    // TDLog.v( "merge next leg: block " + blk.mId + " leg " + id );
    if ( id >= 0 && id != blk.mId ) {
      // mDataAdapter.updateBlockName( id, "", "" ); // name has already been updated in DB
      updateDisplay(); // FIXME change only block with id and blk.mId
    }
    return id;
  }

  // ------------------------------------------------------------------
  public void doProjectionDialog( String name, String start )
  {
    new ProjectionDialog( mActivity, this, TDInstance.sid, name, start ).show();
  }

  void doProjectedProfile( String name, String start, int azimuth )
  {
    makeNewPlot( name, start, false, azimuth );
  }

  void deletePlot( long pid1, long pid2 )
  {
    mApp_mData.deletePlot( pid1, TDInstance.sid );
    mApp_mData.deletePlot( pid2, TDInstance.sid );
    // FIXME NOTIFY
  }

  public boolean hasSurveyPlot( String name )
  {
    return mApp_mData.hasSurveyPlot( TDInstance.sid, name+"p" );
  }
 
  public void makeNewPlot( String name, String start, boolean extended, int project )
  {
    long mPIDp = mApp.insert2dPlot( TDInstance.sid, name, start, extended, project );

    if ( mPIDp >= 0 ) {
      long mPIDs = mPIDp + 1L; // FIXME !!! this is true but not guaranteed
      startDrawingWindow( start, name+"p", mPIDp, name+"s", mPIDs, PlotType.PLOT_PLAN, start, false ); // default no-landscape
    // } else {
    //   TDToast.makeBad( R.string.plot_duplicate_name );
    }
    // updateDisplay( );
  }

/* FIXME_SKETCH_3D 
  public void makeNewSketch3d( String name, String st1, String st2 )
  {
    // FIXME xoffset yoffset, east south and vert (downwards)
    if ( st2 != null ) {
      if ( ! mApp_mData.hasShot( TDInstance.sid, st1, st2 ) ) {
        TDToast.makeBad( R.string.no_shot_between_stations );
        return;
      }
    } else {
      st2 = mApp_mData.nextStation( TDInstance.sid, st1 );
    }
    if ( st2 != null ) {
      float e = 0.0f; // NOTE (e,s,v) are the coord of station st1, and st1 is taken as the origin of the ref-frame
      float s = 0.0f;
      float v = 0.0f;
      long mPID = mApp_mData.insertSketch3d( TDInstance.sid, -1L, name, 0L, st1, st1, st2,
                                            0, // mApp.mDisplayWidth/(2*TopoDroidApp.mScaleFactor),
                                            0, // mApp.mDisplayHeight/(2*TopoDroidApp.mScaleFactor),
                                            10 * TopoDroidApp.mScaleFactor,
                                            0, 0, 10 * TopoDroidApp.mScaleFactor,
                                            0, 0, 10 * TopoDroidApp.mScaleFactor,
                                            e, s, v, 180, 0 );
      if ( mPID >= 0 ) {
        startSketchWindow( name );
      }
    } else {
      TDToast.makeBad( R.string.no_to_station );
    }
  }
 
  void startSketchWindow( String name )
  {
    if ( TDInstance.sid < 0 ) {
      TDToast.make( R.string.no_survey );
      return;
    }

    if ( ! mApp_mData.hasSketch3d( TDInstance.sid, name ) ) {
      TDToast.makeBad( R.string.no_sketch );
      return;
    }

    // notice when starting the SketchWindow the remote device is disconnected 
    // FIXME mApp.disconnectRemoteDevice();

    // TODO
    Intent sketchIntent = new Intent( Intent.ACTION_VIEW ).setClass( this, SketchWindow.class );
    sketchIntent.putExtra( TDTag.TOPODROID_SURVEY_ID, TDInstance.sid );
    sketchIntent.putExtra( TDTag.TOPODROID_SKETCH_NAME, name );
    startActivity( sketchIntent );
  }
 * END_SKETCH_3D */

  // called either by a long-tap on plot button 
  //        or a highlights
  void startExistingPlot( String name, long type, String station ) // name = plot/sketch3d name
  {
    // TDLog.Log( TDLog.LOG_SHOT, "start Existing Plot \"" + name + "\" type " + type + " sid " + TDInstance.sid );
    if ( type != PlotType.PLOT_SKETCH_3D ) {
      PlotInfo plot1 =  mApp_mData.getPlotInfo( TDInstance.sid, name+"p" );
      if ( plot1 != null ) {
        TDInstance.setRecentPlot( name, type );
        PlotInfo plot2 =  mApp_mData.getPlotInfo( TDInstance.sid, name+"s" );
        startDrawingWindow( plot1.start, plot1.name, plot1.id, plot2.name, plot2.id, type, station, plot1.isLandscape() );
        return;
      } else {
        TDInstance.setRecentPlot( null, 0L );
      }
/* FIXME_SKETCH_3D *
    } else {
      Sketch3dInfo sketch = mApp_mData.getSketch3dInfo( TDInstance.sid, name );
      if ( sketch != null ) {
        startSketchWindow( sketch.name );
        return;
      }
 * END_SKETCH_3D */
    }
    TDToast.makeBad( R.string.plot_not_found );
  }

  /** start the plot-sketching window
   * @param start      origin station
   * @param plot1_name plan view name
   * @param plot1_id   plan view id
   * @param plot2_name profile view name
   * @param plot2_id   profile view id
   * @param type       type of plot to show in the window
   * @param station    ???
   * @param landscape  whether to use landscape presentation
   */
  private void startDrawingWindow( String start, String plot1_name, long plot1_id,
                                   String plot2_name, long plot2_id, long type, String station, boolean landscape )
  {
    if ( TDInstance.sid < 0 || plot1_id < 0 || plot2_id < 0 ) {
      TDToast.makeWarn( R.string.no_survey );
      return;
    }
    for ( int k = 0; k < 5; ++k ) {
      if ( BrushManager.hasSymbolLibraries() ) break;
      TDUtil.slowDown( 250 );
    }
    if ( ! BrushManager.hasSymbolLibraries() ) {
      TDToast.makeWarn( R.string.no_symbols );
      return;
    }
    
    // notice when starting the DrawingWindow the remote device is disconnected 
    // FIXME mApp.disconnectRemoteDevice();
    
    Intent drawIntent = new Intent( Intent.ACTION_VIEW ).setClass( this, DrawingWindow.class );
    drawIntent.putExtra( TDTag.TOPODROID_SURVEY_ID, TDInstance.sid );
    drawIntent.putExtra( TDTag.TOPODROID_PLOT_NAME, plot1_name );
    drawIntent.putExtra( TDTag.TOPODROID_PLOT_NAME2, plot2_name );
    drawIntent.putExtra( TDTag.TOPODROID_PLOT_TYPE, type );
    drawIntent.putExtra( TDTag.TOPODROID_PLOT_FROM, start );
    drawIntent.putExtra( TDTag.TOPODROID_PLOT_TO, "" );
    drawIntent.putExtra( TDTag.TOPODROID_PLOT_AZIMUTH, 0.0f );
    drawIntent.putExtra( TDTag.TOPODROID_PLOT_CLINO, 0.0f );
    drawIntent.putExtra( TDTag.TOPODROID_PLOT_MOVE_TO, ((station==null)? "" : station) );
    drawIntent.putExtra( TDTag.TOPODROID_PLOT_LANDSCAPE, landscape );
    // drawIntent.putExtra( TDTag.TOPODROID_PLOT_ID, plot1_id ); // not necessary
    // drawIntent.putExtra( TDTag.TOPODROID_PLOT_ID2, plot2_id ); // not necessary

    startActivity( drawIntent );
  }

  /** react to a change in the configuration
   * @param cfg   new configuration
   */
  @Override
  public void onConfigurationChanged( Configuration new_cfg )
  {
    super.onConfigurationChanged( new_cfg );
    TDLocale.resetTheLocale();
  }

  // ------------------------------------------------------------------
  /** start the audio dialog for a data block
   * @param blk   data block
   */
  void startAudio( DBlock blk )
  {
    (new AudioDialog( mActivity, /* this */ null, blk.mId, blk )).show();
  }

}
