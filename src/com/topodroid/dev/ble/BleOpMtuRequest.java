/* @file BleOpMtuRequest.java
 *
 * @author marco corvi
 * @date jan 2021
 *
 * @brief Bluetooth LE MTU request operation 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.ble;

import com.topodroid.utils.TDLog;

import android.content.Context;

public class BleOpMtuRequest extends BleOperation 
{
  public BleOpMtuRequest( Context ctx, BleComm pipe )
  {
    super( ctx, pipe );
  }

  // public String name() { return "MtuReq"; }

  @Override 
  public void execute()
  {
    TDLog.Log( TDLog.LOG_BT, "BleOp exec MTU request" );
  }
}
