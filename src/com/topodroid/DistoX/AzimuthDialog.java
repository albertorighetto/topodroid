/* @file AzimuthDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid survey azimuth dialog 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDMath;
// import com.topodroid.utils.TDLog;
// import com.topodroid.ui.MyTurnBitmap;
import com.topodroid.ui.MyDialog;
import com.topodroid.ui.MyCheckBox;
import com.topodroid.ui.TDLayout;
import com.topodroid.prefs.TDSetting;

import java.util.Locale;

import android.os.Bundle;

import android.content.Context;

import android.widget.EditText;
import android.widget.Button;
import android.view.View;

import android.text.TextWatcher;
import android.text.Editable;

import android.widget.LinearLayout;
import android.widget.SeekBar;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;

class AzimuthDialog extends MyDialog
                    implements View.OnClickListener
                    , IBearingAndClino
{
  // FIXME_AZIMUTH_DIAL
  // there are two attempts to the azimuth dial 
  // the first (1) uses a bitmap, the second (2) a turn-bitmap

  private final ILister mParent;
  private float mAzimuth;
  private Bitmap mBMdial;
  // private MyTurnBitmap mDialBitmap;

  private EditText mETazimuth;

  // private Button mBTback;
  // private Button mBTfore;
  private Button mBTazimuth;
  private Button mBTsensor;
  private Button mBTok;
  private Button mBTleft;
  private Button mBTright;

  private Button mBtnCancel;

  private SeekBar mSeekBar;

  // AzimuthDialog( Context context, ILister parent, float azimuth, MyTurnBitmap dial ) // FIXME_AZIMUTH_DIAL 2
  AzimuthDialog( Context context, ILister parent, float azimuth, Bitmap dial ) // FIXME_AZIMUTH_DIAL 1
  {
    super(context, R.string.AzimuthDialog );
    mParent  = parent;
    mAzimuth = azimuth;
    // mDialBitmap = dial;
    mBMdial = dial;
  }

  static Bitmap getRotatedBitmap( float azimuth, Bitmap source )
  {
    Matrix m = new Matrix();
    m.preRotate( azimuth - 90 );
    // float s = TDMath.cosd( ((azimuth % 90) - 45) );
    // m.preScale( s, s );
    int w = 96; // source.getWidth();
    Bitmap bm1 = Bitmap.createScaledBitmap( source, w, w, true );
    return Bitmap.createBitmap( bm1, 0, 0, w, w, m, true);
    // rotatedBitmap( source, source.getWidth(), mAzimuth, 96 );
    // Bitmap bm2 = Bitmap.createBitmap( mPxl, 96, 96, Bitmap.Config.ALPHA_8 );
  }

  private void updateView()
  {
    Bitmap bm2 = getRotatedBitmap( mAzimuth, mBMdial );
    mBTazimuth.setBackgroundDrawable( new BitmapDrawable( mContext.getResources(), bm2 ) ); // DEPRECATED API-16

    // Bitmap bm2 = mDialBitmap.getBitmap( mAzimuth, 96 );
    // TDandroid.setButtonBackground( mBTazimuth, new BitmapDrawable( mContext.getResources(), bm2 ) );
  }

  private void updateEditText() { mETazimuth.setText( String.format(Locale.US, "%d", (int)mAzimuth ) ); }

  private void updateSeekBar() { mSeekBar.setProgress( ((int)mAzimuth + 180)%360 ); }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    // TDLog.Log( TDLog.LOG_SHOT, "Shot Dialog::onCreate" );
    initLayout( R.layout.azimuth_dialog, R.string.title_azimut );

    // mBTback = (Button) findViewById(R.id.btn_back );
    // mBTfore = (Button) findViewById(R.id.btn_fore );
    mBTazimuth = (Button) findViewById(R.id.btn_azimuth );
    // mBTsensor  = (Button) findViewById(R.id.btn_sensor );
    mBTok      = (Button) findViewById(R.id.btn_ok );
    mBTleft    = (Button) findViewById(R.id.btn_left );
    mBTright   = (Button) findViewById(R.id.btn_right );

    mBtnCancel = (Button) findViewById( R.id.button_cancel );
    mBtnCancel.setOnClickListener( this );

    mSeekBar  = (SeekBar) findViewById( R.id.seekbar );
    mETazimuth = (EditText) findViewById( R.id.et_azimuth );

    mETazimuth.addTextChangedListener( new TextWatcher() {
      @Override
      public void afterTextChanged( Editable e ) { }

      @Override
      public void beforeTextChanged( CharSequence cs, int start, int cnt, int after ) { }

      @Override
      public void onTextChanged( CharSequence cs, int start, int before, int cnt ) 
      {
        try {
          int azimuth = Integer.parseInt( mETazimuth.getText().toString() );
          if ( azimuth < 0 || azimuth > 360 ) azimuth = 0;
          mAzimuth = azimuth;
          updateSeekBar();
          updateView();
        } catch ( NumberFormatException e ) { }
      }
    } );

    LinearLayout layout4 = (LinearLayout) findViewById( R.id.layout4 );
    int size = TDSetting.mSizeButtons; // TopoDroidApp.getScaledSize( mContext );
    layout4.setMinimumHeight( size + 40 );

    LinearLayout.LayoutParams lp = TDLayout.getLayoutParams( 0, 10, 20, 10 );

    mBTsensor = new MyCheckBox( mContext, size, R.drawable.iz_compass_transp, R.drawable.iz_compass_transp ); 
    layout4.addView( mBTsensor, lp );

    // mBTback.setOnClickListener( this );
    // mBTfore.setOnClickListener( this );
    mBTazimuth.setOnClickListener( this );
    mBTsensor.setOnClickListener( this );
    mBTok.setOnClickListener( this );
    mBTleft.setOnClickListener( this );
    mBTright.setOnClickListener( this );

    mSeekBar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
      public void onProgressChanged( SeekBar seekbar, int progress, boolean fromUser) {
        if ( fromUser ) {
          setBearingAndClino( (progress+180)%360, 0, 0 ); // clino 0, orientation 0
        }
      }
      public void onStartTrackingTouch(SeekBar seekbar) { }
      public void onStopTrackingTouch(SeekBar seekbar) { }
    } );
    mSeekBar.setMax( 360 );

    updateSeekBar();
    updateView();
    updateEditText();
  }

  public void setBearingAndClino( float b0, float c0, int o0 )
  {
    // TDLog.v( "Azimuth dialog set orientation " + o0 + " bearing " + b0 + " clino " + c0 );
    mAzimuth = b0;
    updateView();
    updateEditText();
  }

  public boolean setJpegData( byte[] data ) { return false; }

  private TimerTask mTimer = null;

  public void onClick(View v) 
  {
    if ( mTimer != null ) {
      mTimer.cancel( true );
      mTimer = null;
    }

    int id = v.getId();
    if ( id == R.id.button_cancel ) {
      dismiss();
    } else if ( id == R.id.btn_azimuth ) {
      // mAzimuth += 90; if ( mAzimuth >= 360 ) mAzimuth -= 360;
      mAzimuth = TDMath.add90( mAzimuth );
      updateSeekBar();
      updateView();
      updateEditText();
    // } else if ( id == mBTsensor.getId() ) {
    } else if ( (Button)v == mBTsensor ) {
      mTimer = new TimerTask( this, TimerTask.Y_AXIS, TDSetting.mTimerWait, 10 );
      mTimer.execute();
    } else if ( id == R.id.btn_ok ) {
      mParent.setRefAzimuth( mAzimuth, 0 );
      dismiss();
    } else if ( id == R.id.btn_left ) {
      mParent.setRefAzimuth( mAzimuth, -1L );
      dismiss();
    } else if ( id == R.id.btn_right ) {
      mParent.setRefAzimuth( mAzimuth, 1L );
      dismiss();
    // } else if ( id == R.id.btn_back ) {
    //   mAzimuth = TDMath.in360( mAzimuth-5 );
    //   updateSeekBar();
    //   updateView();
    //   updateEditText();
    // } else if ( id == R.id.btn_fore ) {
    //   mAzimuth = TDMath.in360( mAzimuth+5 );
    //   updateSeekBar();
    //   updateView();
    //   updateEditText();
    } else {
      dismiss();
    }
  }

  @Override
  public void onBackPressed()
  {
    if ( mTimer != null ) {
      mTimer.cancel( true );
      mTimer = null;
    }
    dismiss();
  }

}

