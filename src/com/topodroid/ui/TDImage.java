/* @file TDImage.java
 *
 * @author marco corvi
 * @date july 2018
 *
 * @brief TopoDroid image utility
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.ui;

import com.topodroid.utils.TDLog;
import com.topodroid.prefs.TDSetting;

import java.io.IOException;

import android.os.Build;
import android.widget.ImageView;

import android.graphics.Matrix;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface; // REQUIRES android.support

public class TDImage
{
  private String mFilename;
  private float  mAzimuth = 0;
  private float  mClino   = 0;
  private int mOrientation = 0;
  private String mDate = "";

  private Bitmap mImage    = null;
  private Bitmap mImage2   = null;
  private int mImageWidth  = 0;
  private int mImageHeight = 0;

  public TDImage( String filename )
  {
    mFilename = filename;
    readExif();
    decodeImage();
    // TDLog.v( "TD image " + filename );
  }

  public float azimuth() { return mAzimuth; }
  public float clino()   { return mClino; }
  public String date()   { return mDate; }
  public int width()     { return mImageWidth; }
  public int height()    { return mImageHeight; }

  private void decodeImage()
  {
    BitmapFactory.Options bfo = new BitmapFactory.Options();
    bfo.inJustDecodeBounds = true;
    BitmapFactory.decodeFile( mFilename, bfo );
    int required_size = TDSetting.mThumbSize;
    // TDLog.v( "photo: file " + mFilename + " " + bfo.outWidth + "x" + bfo.outHeight + " req. size " + required_size );
    int scale = 1;
    while ( bfo.outWidth/scale > required_size || bfo.outHeight/scale > required_size ) {
      scale *= 2;
    }
    bfo.inJustDecodeBounds = false;
    bfo.inSampleSize = scale;
    mImage = BitmapFactory.decodeFile( mFilename, bfo );
    if ( mImage != null ) {
      mImageWidth   = mImage.getWidth();
      mImageHeight  = mImage.getHeight();
    }  
    // TDLog.v( "photo: file " + mFilename + " image " + mImageWidth + "x" + mImageHeight + " req. size " + required_size + " scale " + scale );
  }

  private void readExif()
  {
    try {
      ExifInterface exif = new ExifInterface( mFilename );
      // mAzimuth = exif.getAttribute( "GPSImgDirection" );
      mOrientation = exif.getAttributeInt( ExifInterface.TAG_ORIENTATION, 0 );
      // TDLog.v( "Photo edit orientation " + mOrientation );
      String b = exif.getAttribute( ExifInterface.TAG_GPS_LONGITUDE );
      String c = exif.getAttribute( ExifInterface.TAG_GPS_LATITUDE );
      mDate = exif.getAttribute( ExifInterface.TAG_DATETIME );
      // TDLog.v( "TD image bearing " + b + " clino " + c + " date " + mDate );
      if ( mDate == null ) mDate = "";
      if ( b == null || c == null ) { // FIXME-GPS_LATITUDE work-around for tag GPSLatitude not working
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
          String u = exif.getAttribute( ExifInterface.TAG_IMAGE_DESCRIPTION );
          // TDLog.v( "Photo desc " + u );
          if ( u != null ) {
            String[] vals = u.split(" ");
            if (vals.length > 1) {
              if (b == null) b = vals[0] + "/100";
              if (c == null) c = vals[1] + "/100";
            }
          }
        }
      }
      if ( b != null && c != null ) {
        // TDLog.v( "b " + b + " c " + c );
        int k = b.indexOf('/');
	if ( k >= 0 ) {
          try { mAzimuth = Integer.parseInt( b.substring(0,k) ) / 100.0f; } catch ( NumberFormatException e ) { }
	}
        k = c.indexOf('/');
	if ( k >= 0 ) {
          try { mClino = Integer.parseInt( c.substring(0,k) ) / 100.0f; } catch ( NumberFormatException e ) { }
	}
        // TDLog.v( "Long <" + mAzimuth + "> Lat <" + mClino + ">" );
      }
    } catch ( IOException e ) {
      TDLog.Error("failed exif interface " + mFilename );
    }
  }

  // 6 = upward
  // 8 = downward
  // 1 = leftward
  // 3 = rightward
  public boolean isPortrait() { return mOrientation == 6 || mOrientation == 8; }

  // @param view
  // @param ww     width
  // @param hh     height
  // @param orient whether to apply image orientation
  public boolean fillImageView( ImageView view, int ww, int hh, boolean orient )
  {
    if ( mImage == null ) return false;

    int h = mImageHeight * ww / mImageWidth;
    if ( h > hh ) {
      ww = mImageWidth * hh / mImageHeight;
    } else {
      hh = h;
    }

    // if ( isPortrait() ) {
    //   hh = (int)( mImageHeight * ww / mImageWidth );
    // } else {
    //   ww = (int)( mImageWidth * hh / mImageHeight );
    // }
      
    // TDLog.v( "fill image view w " + ww + " h " + hh + " orientation " + mOrientation );
    if ( ww <= 0 || hh <= 0 ) return false;
    if ( mImage2 != null ) mImage2.recycle();
    mImage2 = Bitmap.createScaledBitmap( mImage, ww, hh, true );
    if ( mImage2 == null ) return false;

    // MyBearingAndClino.applyOrientation( view, mImage2, (orient? mOrientation : 6) );
    applyOrientation( view, mImage2, mOrientation );
    return true;
  }

  // full image view: fixed orientation
  // boolean fillImageView( ImageView view ) 
  // { 
  //   return fillImageView( view, (int)(TopoDroidApp.mDisplayWidth), (int)(TopoDroidApp.mDisplayHeight), false );
  // }

  public void recycleImages()
  {
    if ( mImage != null ) mImage.recycle();
    if ( mImage2 != null ) mImage2.recycle();
  }
  
  // EXIF orientation is detailed in MyBearingAndClino
  //                           up
  // 1: no rotation            6
  // 6: rotate right    left 1-+-3 right
  // 3: rotate 180             8
  // 8: rotate left            down
  private static void applyOrientation( ImageView image, Bitmap bitmap, int orientation )
  {
    Matrix m = new Matrix();
    int w = bitmap.getWidth();
    int h = bitmap.getHeight();
    if ( orientation == 6 ) {
      m.setRotate(  90f, (float)w/2, (float)h/2 );
      // image.setRotation(  90 ); // REQUIRES API-14
    } else if ( orientation == 3 ) {
      m.setRotate( 180f, (float)w/2, (float)h/2 );
      // image.setRotation( 180 );
    } else if ( orientation == 8 ) {
      m.setRotate( 270f, (float)w/2, (float)h/2 );
      // image.setRotation( 270 );
    } else {
      image.setImageBitmap( bitmap );
      return;
    }
    image.setImageBitmap( Bitmap.createBitmap( bitmap, 0, 0, w, h, m, true ) );
  }
}
