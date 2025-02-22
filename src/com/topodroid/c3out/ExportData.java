/* @file ExportData.java
 *
 * @author marco corvi
 * @date apr 2021
 *
 * @brief export data struct
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3out;

public class ExportData
{
  public String  mName = null;
  public boolean mSplays;
  public boolean mWalls;
  public boolean mSurface;
  public boolean mStation;
  public boolean mOverwrite = true;
  public int mType;
  public String mMime; // mime type
  public String mExt;

  /** cstr
   * @param name      survey name
   * @param splays    include splays 
   * @param walls     include walls
   * @param surface   include surface
   * @param station   include stations
   * @param overwrite (unused)
   */
  public ExportData( String name, boolean splays, boolean walls, boolean surface, boolean station, boolean overwrite )
  {
    mName     = name;
    mType     = 0; // ModelType.NONE
    mExt      = "";
    mSplays   = splays;
    mWalls    = walls;
    mStation  = station;
    mSurface  = surface;
    mOverwrite = overwrite;
    mMime     = "application/octet-stream";
  }

  /** copy cstr
   * @param name      survey name
   * @param export    export to copy from
   */
  public ExportData( String name, ExportData export )
  {
    if ( name != null ) mName = name;
    mType     = export.mType;
    mExt      = export.mExt;
    mSplays   = export.mSplays;
    mWalls    = export.mWalls;
    mStation  = export.mStation;
    mSurface  = export.mSurface;
    mOverwrite = export.mOverwrite;
    mMime     = export.mMime;
  }

}
