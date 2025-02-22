/** @file TdmConfig.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid Manager cave-project object
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDVersion;
import com.topodroid.DistoX.TDUtil;

// import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
// import java.io.FileWriter;
// import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Date;
// import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Iterator;

class TdmConfig extends TdmFile
{
  // String mParentDir;            // parent directory
  String mSurveyName;
  TdmSurvey mSurvey;             // inline survey in the tdconfig file
  private ArrayList< TdmSurvey > mViewSurveys = null; // current view surveys
  private ArrayList< TdmInput >  mInputs; // input surveys
  private ArrayList< TdmEquate > mEquates;
  private boolean mRead;        // whether this Tdm_Config has read the file
  private boolean mSave;        // whether this Tdm_Config needs to be saved

  public TdmConfig( String filepath, boolean save )
  {
    super( filepath, null );
    // TDLog.v("Tdm_Config cstr filepath " + filepath );
    // mParentDir = (new File( filepath )).getParentFile().getName() + "/";
    mSurvey    = null;
    mInputs    = new ArrayList< TdmInput >();
    mEquates   = new ArrayList< TdmEquate >();
    mRead      = false;
    mSave      = save;
    mSurveyName = getNameFromFilepath( filepath );
  }

  public String getSurveyName() { return mSurveyName; }

  void populateViewSurveys( ArrayList< TdmSurvey > surveys )
  {
    mViewSurveys = new ArrayList< TdmSurvey >(); // current view surveys
    for ( TdmSurvey survey : surveys ) {
      // TDLog.v("Populate survey " + survey.getName() );
      survey.reduce();
      mViewSurveys.add( survey );
    }
  }

  void dropEquates( String survey )
  {
    // TDLog.v("drop equates with " + survey + " before " + mEquates.size() );
    if ( survey == null || survey.length() == 0 ) return;
    ArrayList< TdmEquate > equates = new ArrayList<>();
    for ( TdmEquate equate : mEquates ) {
      if ( equate.dropStations( survey ) > 1 ) {
        equates.add( equate );
        setSave();
      }
    }
    mEquates = equates;
    // TDLog.v("dropped equates with " + survey + " after " + mEquates.size() );
  }

  void addEquate( TdmEquate equate ) 
  {
    if ( equate == null ) return;
    mEquates.add( equate );
    setSave();
    // TDLog.v("nr. equates " + mEquates.size() );
  }

  // unconditionally remove an equate
  void removeEquate( TdmEquate equate ) 
  { 
    mEquates.remove( equate );
    setSave();
  }
    
  boolean hasInput( String name )
  {
    if ( name == null ) return false;
    // TDLog.v("Tdm_Config check input name " + name );
    for ( TdmInput input : mInputs ) {
      // TDLog.v("Tdm_Config check input " + input.mName );
      if ( name.equals( input.getSurveyName() ) ) return true;
    }
    return false;
  }

  // this is called by readFile
  private void insertInput( String name )
  {
    if ( name == null ) return;
    // TDLog.v( "insert input " + name );
    mInputs.add( new TdmInput( name ) );
  }

  // this is called by the Config activity 
  void addInput( TdmInput input )
  {
    if ( input == null ) return;
    // TDLog.v( "add input " + input.mName );
    mInputs.add( input );
    setSave();
  }

  Iterator getInputsIterator() { return mInputs.iterator(); }

  int getInputsSize() { return mInputs.size(); }

  ArrayList< TdmInput > getInputs() { return mInputs; }

  ArrayList< TdmSurvey > getViewSurveys() { return mViewSurveys; }

  ArrayList< TdmEquate > getEquates() { return mEquates; }

  private void dropInput( String name )
  {
    if ( name == null ) return;
    // TDLog.v( "drop input " + name );
    for ( TdmInput input : mInputs ) {
      if ( name.equals( input.getSurveyName() ) ) {
        mInputs.remove( input );
        setSave();
        return;
      }
    }
  }

  // this is called by the Config Activity
  void setInputs( ArrayList< TdmInput > inputs ) 
  {
    if ( inputs != null ) {
      // TDLog.v( "set inputs " + inputs.size() );
      mInputs = inputs;
      setSave();
    }
  }

  // used also by Config Activity when a source is added
  void setSave() { mSave = true; }

  // ---------------------------------------------------------------
  // this is TDUtil.currentDate()
  // static String currentDate()
  // {
  //   SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
  //   return sdf.format( new Date() );
  // }

  // ---------------------------------------------------------------

  // this is called by the TdmConfigActivity when it goes on pause
  /** write the cave project info to the tdconfig file
   * @param force    whether to force writing
   */
  void writeTdmConfig( boolean force )
  {
    // TDLog.v( "save tdconfig " + this + " " + mSave );
    if ( mSave || force ) { // was mRead || force
      String filepath = getFilepath();
      try {
        BufferedWriter bw = TDFile.getTopoDroidFileWriter( filepath );
        PrintWriter pw = new PrintWriter( bw );
        writeTd( pw );
        bw.close();
      } catch ( IOException e ) { 
        TDLog.Error("Tdm Config write file " + filepath + " I/O error " + e.getMessage() );
      }
      mSave = false;
    }
  }

  /** read the cave project info from the tdconfig file
   * @note the file is read only once - if already read it is skipped
   */
  void readTdmConfig()
  {
    // TDLog.v( "read tdconfig " + this + " " + mRead );
    if ( mRead ) return;
    readFile();
    mRead = true;
  }

  // ---------------------------------------------------------
  // READ and WRITE

  /** write the cave project info to a tdconfig file
   * @param pw   file writer
   */
  private void writeTd( PrintWriter pw ) throws IOException
  {
    // TDLog.v("save config " + mSurveyName + " " + filepath );
    pw.format("# created by TopoDroid Manager %s - %s\n", TDVersion.string(), TDUtil.currentDate() );
    pw.format("source\n");
    pw.format("  survey %s\n", mSurveyName );
    for ( TdmInput input : mInputs ) {
      // FIXME path
      String path = input.getSurveyName();
      // TDLog.v("config write add survey " + path );
      pw.format("    load %s\n", path );
    }
    for ( TdmEquate equate : mEquates ) {
      pw.format("    equate");
      for ( String st : equate.mStations ) pw.format(" %s", st );
      pw.format("\n");
    }
    pw.format("  endsurvey\n");
    pw.format("endsource\n");
  }

  /** extract the project name from a filepath
   * @param filepath    file pathname
   * @return the project name, ie, the name before the extension (namely ".tdconfig")
   */
  private String getNameFromFilepath( String filepath )
  {
    int start = filepath.lastIndexOf('/') + 1;
    int end   = filepath.lastIndexOf('.');
    return ( end > start )? filepath.substring( start, end ) : filepath.substring( start );
  }

  /** read the config file
   * @note if the file does not exists creates it and write an empty tdconfig file
   */
  private void readFile( )
  {
    String filepath = getFilepath();
    try {
      BufferedReader br = TDFile.getTopoDroidFileReader( filepath );
      if ( br == null ) { // file does not exists (or is not readable)
        TDLog.v("file no-exist or no-read: " + filepath );
        mSurveyName = getNameFromFilepath( filepath );
        writeTdmConfig( true );
        return;
      }

      TDLog.v( "read config " + filepath );
      String line = br.readLine();
      int cnt = 1;
      // TDLog.v( Integer.toString(cnt) + ":" + line );
      while ( line != null ) {
        line = line.trim();
        int pos = line.indexOf( '#' );
        if ( pos >= 0 ) line = line.substring( 0, pos );
        if ( line.length() > 0 ) {
          String[] vals = line.split( " " );
          if ( vals.length > 0 ) {
            if ( vals[0].equals( "source" ) ) {
            } else if ( vals[0].equals( "survey" ) ) {
              for (int k=1; k<vals.length; ++k ) {
                if ( vals[k].length() > 0 ) {
                  mSurveyName = vals[k];
                  break;
                }
              }
            } else if ( vals[0].equals( "load" ) ) {
              for (int k=1; k<vals.length; ++k ) {
                if ( vals[k].length() > 0 ) {
                  String surveyname = vals[k];
                  insertInput( surveyname );
                  break;
                }
              }    
            } else if ( vals[0].equals( "equate" ) ) {
              TdmEquate equate = new TdmEquate();
              for (int k=1; k<vals.length; ++k ) {
                if ( vals[k].length() > 0 ) {
                  equate.addStation( vals[k] );
                }
              }
              mEquates.add( equate );
            }
          }
        }
        line = br.readLine();
        ++ cnt;
      }
      br.close();
    } catch ( IOException e ) {
      // TODO
      TDLog.Error( "TdManager exception " + e.getMessage() );
    }
    // TDLog.v( "Tdm_Config read file: nr. sources " + mInputs.size() );
  }
 
  // ---------------------------------------------------------
  // EXPORT

  /** export the project to therion format
   * @param overwrite  whether to overwrite the output file (if it exists)
   * @param bw         buffered writer
   * @return non-null string if success
   */
  String exportTherion( boolean overwrite, PrintWriter bw ) throws IOException
  {
    bw.format("# created by TopoDroid Manager %s - %s\n", TDVersion.string(), TDUtil.currentDate() );
    bw.format("source\n");
    bw.format("  survey %s\n", mSurveyName );
    for ( TdmInput input : mInputs ) {
      // FIXME path
      String path = "../th/" + input.getSurveyName() + ".th";
      // TDLog.v("config write add survey " + path );
      bw.format("    input %s\n", path );
    }
    for ( TdmEquate equate : mEquates ) {
      bw.format("    equate");
      for ( String st : equate.mStations ) bw.format(" %s", st );
      bw.format("\n");
    }
    bw.format("  endsurvey\n");
    bw.format("endsource\n");
    return "thconfig";
  }

  /** export the project to survex format
   * @param overwrite  whether to overwrite the output file (if it exists)
   * @param bw         buffered writer
   * @return non-null string if success
   */
  String exportSurvex( boolean overwrite, PrintWriter bw ) throws IOException
  {
    bw.format("; created by TopoDroid Manager %s - %s\n", TDVersion.string(), TDUtil.currentDate() );
    // TODO EXPORT
    for ( TdmInput s : mInputs ) {
      String path = "../svx/" + s.getSurveyName() + ".svx";
      bw.format("*include %s\n", path );
    }
    for ( TdmEquate equate : mEquates ) {
      bw.format("*equate");
      for ( String st : equate.mStations ) bw.format(" %s", toSvxStation( st ) );
      bw.format("\n");
    }
    return "survex";
  }

  /** convert a station name from therion to survex syntax
   * @param st   therion name of the station
   * @return survex name of the station
   */
  private String toSvxStation( String st )
  {
    int pos = st.indexOf('@');
    return st.substring(pos+1) + "." + st.substring(0,pos);
  }

}
