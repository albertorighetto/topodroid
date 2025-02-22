/* @file FixedInfo.java
 *
 * @author marco corvi
 * @date apr 2012
 *
 * @brief TopoDroid fixed stations (GPS-localized stations)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import androidx.annotation.RecentlyNonNull;

import com.topodroid.utils.TDLog;
import com.topodroid.prefs.TDSetting;

import com.topodroid.mag.MagLatLong;

import java.util.Locale;

/** fixed (GPS) point
 * Note the order of data: LONGITUDE - LATITUDE - ALTITUDE
 */
public class FixedInfo extends MagLatLong
{
  public final static long SRC_UNKNOWN    = 0L;
  public final static long SRC_TOPODROID  = 1L;
  public final static long SRC_MANUAL     = 2L;
  public final static long SRC_MOBILE_TOP = 3L;
  long   id;       // fixed id
  long   source;   // 0: unknown,  1: topodroid,  2: manual,   3: mobile-topographer
  String name;     // station name, or whatever
  // double lat;      // wgs84 latitude [decimal deg] (from MagLatLong)
  // double lng;      // wgs84 longitude [decimal deg]
  double alt;      // wgs84 altitude [m]
  double asl;      // geoid altitude [m] 
  String comment;
  String cs;       // coordinate system
  double cs_lng;   // longitude / east
  double cs_lat;   // latitude / north
  double cs_alt;   // altitude
  long   cs_n_dec; // number of decimals in lng/lat

  public FixedInfo( long _id, String n, double longitude, double latitude, double h_ellip, double h_geoid, String cmt, long src )
  {
    id = _id;
    name = n;
    lat = latitude;
    lng = longitude;
    alt = h_ellip;
    asl = h_geoid;
    comment = cmt;
    source  = src;
    clearConverted();
  }

  FixedInfo( long _id, String n, double longitude, double latitude, double h_ellip, double h_geoid,
                    String cmt, long src, String name_cs, double lng_cs, double lat_cs, double alt_cs, long n_dec )
  {
    id = _id;
    name = n;
    lat = latitude;
    lng = longitude;
    alt = h_ellip;
    asl = h_geoid;
    comment = cmt;
    source  = src;
    cs      = name_cs;
    cs_lng  = lng_cs;
    cs_lat  = lat_cs;
    cs_alt  = alt_cs;
    cs_n_dec = (n_dec >= 0)? n_dec : 0;
  }

  /** set converted coordinates
   * @param name_cs   coordinate system
   * @param lng_cs    longitude / east
   * @param lat_cs    latitude / north
   * @param alt_cs    altitude
   * @param n_dec     number of decimals in lng/lat
   */
  void setCSCoords( String name_cs, double lng_cs, double lat_cs, double alt_cs, long n_dec )
  {
    cs = name_cs;
    if ( cs != null && cs.length() > 0 ) {
      cs_lng = lng_cs;
      cs_lat = lat_cs;
      cs_alt = alt_cs;
      cs_n_dec = (n_dec >= 0)? n_dec : 0;
    }
  }

  /** reset converted coordinates
   */
  void clearConverted()
  {
    cs = null;
    cs_lng = 0;
    cs_lat = 0;
    cs_alt = 0;
    cs_n_dec = 2L;
  }

  /** test if this fixed has converted coordinates
   * @return true if converted coord system is not null
   */
  boolean hasCSCoords() { return ( cs != null && cs.length() > 0 ); }

  // public FixedInfo( long _id, String n, double longitude, double latitude, double h_ellip, double h_geoid )
  // {
  //   id = _id;
  //   name = n;
  //   lat = latitude;
  //   lng = longitude;
  //   alt = h_ellip;
  //   asl = h_geoid;
  //   comment = "";
  // }

  // get the string "name long lat alt" for the exports
  String toExportString()
  {
    return String.format(Locale.US, "%s %.6f %.6f %.0f", name, lng, lat, asl );
  }

  String toExportCSString()
  {
    StringBuilder fmt = new StringBuilder();
    fmt.append("%s %.").append( cs_n_dec ).append("f %.").append( cs_n_dec ).append("f %.0f");
    return String.format(Locale.US, fmt.toString(), name, cs_lng, cs_lat, cs_alt );
    // return String.format(Locale.US, "%s %.2f %.2f %.0f", name, cs_lng, cs_lat, cs_alt );
  }

  String csName() { return cs; }

  public String getComment() { return comment; }

  public String getSource() 
  {
    switch ( (int)source ) {
      case 1: return "TopoDroid";
      case 2: return "manual";
      case 3: return "Mobile-Topogra[her";
    }
    return "unknown";
  }


  // @RecentlyNonNull
  public String toString()
  {
    return name + " " + double2string( lng ) + " " + double2string( lat ) + " " + (int)(asl) + " [wgs " + (int)(alt) + "]";
  }

  static String double2string( double x )
  {
    return ( TDSetting.mUnitLocation == TDUtil.DDMMSS ) ? double2ddmmss( x ) : double2degree( x );
  }

  static String double2ddmmss( double x )
  {
    int dp = (int)x;
    x = 60*(x - dp);
    int mp = (int)x;
    x = 60*(x - mp);
    int sp = (int)x;
    int ds = (int)( 100 * (x-sp) + 0.4999 );
    return String.format(Locale.US, "%d°%02d'%02d.%02d", dp, mp, sp, ds );
  }

  static String double2degree( double x )
  {
    return String.format(Locale.US, "%.6f", x );
  }

  static double string2double( CharSequence txt )
  {
    if ( txt == null ) return -1111;
    return string2double( txt.toString() );
  }

  static double string2double( String str )
  {
    if ( str == null ) return -1111.0;
    str = str.trim();                  // drop initial and final spaces
    if ( str.length() == 0 ) return -1111.0;

    str = str.replace( " ", ":" );     // replace separators
    str = str.replace( "°", ":" );     
    str = str.replace( "'", ":" );     
    str = str.replace( "/", "." );
    str = str.replace( ",", "." );
    String[] token = str.split( ":" ); // tokenize str on ':'
    try {
      if ( token.length == 3 ) {
        return Integer.parseInt( token[0] )
             + Integer.parseInt( token[1] ) / 60.0
             + Double.parseDouble( token[2] ) / 3600.0;
      } else if ( token.length == 1 ) {
        return Double.parseDouble( str );
      }
    } catch (NumberFormatException e ) {
      TDLog.Error( "string2double parse error: " + str );
    }
    return -1111.0; // more neg than -1000
  }        

  static double string2real( CharSequence txt )
  {
    if ( txt == null ) return 0;
    return string2real( txt.toString() );
  }

  static private double string2real( String str )
  {
    if ( str == null ) return 0;
    str = str.trim();  // drop initial and final spaces
    if ( str.length() == 0 ) return 0;
    str = str.replace( "/", "." );
    str = str.replace( ",", "." );
    try {
      return Double.parseDouble( str );
    } catch (NumberFormatException e ) {
      TDLog.Error( "string2real parse error: " + str );
    }
    return 0;
  }        

}
