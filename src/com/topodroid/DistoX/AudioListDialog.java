/* @file AudioListDialog.java
 *
 * @author marco corvi
 * @date jul 2020
 *
 * @brief TopoDroid audio list dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.ui.MyDialog;
import com.topodroid.utils.TDLog;

// import android.util.Log;

import android.os.Bundle;
import android.content.Context;

// import android.widget.Button;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.view.View;
import android.media.MediaPlayer;

import java.util.List;

import java.io.File;
import java.io.IOException;

class AudioListDialog extends MyDialog
                  implements OnItemClickListener
{
  private ListView mList;
  // private Button   mButtonCancel;

  private List< AudioInfo > mAudios;
  private List< DBlock > mShots;
  private MediaPlayer mMP = null;

  /**
   * @param context   context
   */
  AudioListDialog( Context context, List< AudioInfo > audios, List< DBlock > shots )
  {
    super( context, R.string.AudioListDialog );
    mAudios = audios;
    mShots  = shots;
  }


  private String getAudioDescription( AudioInfo audio )
  {
    if ( audio.shotid >= 0 ) {
      for ( DBlock blk : mShots ) if ( blk.mId == audio.shotid ) {
        return audio.getFullString( blk.mFrom + " " + blk.mTo );
      }
    }
    return audio.getFullString( "- -" );
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout(R.layout.audio_list_dialog, R.string.title_audio_list );

    mList = (ListView) findViewById( R.id.list );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );
    // ArrayList< String > names = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>( mContext, R.layout.message );
    for ( AudioInfo af : mAudios ) { 
      // names.add( getAudioDescription( af );
      arrayAdapter.add( getAudioDescription( af ) );
    }
    mList.setAdapter( arrayAdapter );
    
    // ( (Button) findViewById(R.id.photo_back ) ).setOnClickListener( this );
  }

  // @Override
  // public void onClick(View v) 
  // {
  //   dismiss();
  // }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    // play audio at pos
    playAudio( pos );
  }

  private void playAudio( int pos )
  {
    AudioInfo audio = mAudios.get( pos );
    if ( audio != null ) { 
      String filepath = TDPath.getSurveyAudioFile( TDInstance.survey, Long.toString( audio.id ) );
      File file = new File( filepath );
      if ( file.exists() ) {
        startPlay( filepath );
      }
    }
  }

  private void startPlay( String filepath )
  {
    // TDToast.make( String.format( mContext.getResources().getString( R.string.playing ), filepath ) );
    try {
      mMP = new MediaPlayer();
      mMP.setDataSource( filepath );
      mMP.setOnCompletionListener( new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion( MediaPlayer mp ) 
        { 
          mp.release();
          mMP = null;
        }
      } );
      mMP.prepare();
      mMP.start();
    } catch ( IllegalStateException e ) {
      TDLog.Error("Illegal state " + e.getMessage() );
    } catch ( IOException e ) {
      TDLog.Error("I/O error " + e.getMessage() );
    }
  }

  @Override
  public void onBackPressed()
  {
    releaseMP();
    super.onBackPressed();
  }

  private void releaseMP()
  {
    if ( mMP != null ) {
      mMP.stop();
      mMP.release();
      mMP = null;
    }
  }

}
