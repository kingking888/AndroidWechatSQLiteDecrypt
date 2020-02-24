package am.yiad.wxsqlpasswordhook;

import android.util.Log;

import java.util.Formatter;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by adamyi on 12/2/17.
 */

public class XModule implements IXposedHookLoadPackage {
    static private String TAG = "wxSqlPwdHook";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (loadPackageParam.packageName.equals("com.tencent.mm")) {

            XposedHelpers.findAndHookMethod(
					"com.tencent.wcdb.database.SQLiteDatabase", 														// 被HOOK对象名
					loadPackageParam.classLoader, 																		// classLoader，固定的值，不用关心
					"openDatabase", 																					// 被HOOK对象的函数名
					String.class,																						// 参数0：数据库全路径
                    byte[].class, 																						// 参数1：用户名密码（一个加密后的7位值）
					loadPackageParam.classLoader.loadClass("com.tencent.wcdb.database.SQLiteCipherSpec"),				// 参数2：是一个SQLiteCipherSpec对象，该对象中包含了加密方式
                    loadPackageParam.classLoader.loadClass("com.tencent.wcdb.database.SQLiteDatabase$CursorFactory"),	// 参数3：某工厂对象
					int.class,																							// 参数4：Flags，未知标记
                    loadPackageParam.classLoader.loadClass("com.tencent.wcdb.DatabaseErrorHandler"),					// 参数5：错误处理的句柄
					int.class,																							// 参数6：加密方式的某个参数PoolSize
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
							// 参数0：数据库全路径
                            Log.i(TAG, "Path: " + param.args[0]);
							// 参数1：用户名密码（一个加密后的7位值）
                            Log.i(TAG, "Password: " + new String((byte[]) param.args[1], "UTF-8"));
                            Formatter formatter = new Formatter();
                            for (byte b : (byte[]) param.args[1]) {
                                formatter.format("%02x", b);
                            }
                            Log.i(TAG, "Password (hex): 0x" + formatter.toString());
							// 参数2：是一个SQLiteCipherSpec对象，该对象中包含了加密方式
                            Log.i(TAG, "CipherSpec - kdfAlgorithm: " + XposedHelpers.getIntField(param.args[2], "kdfAlgorithm"));
                            Log.i(TAG, "CipherSpec - kdfIteration: " + XposedHelpers.getIntField(param.args[2], "kdfIteration"));
                            Log.i(TAG, "CipherSpec - hmacAlgorithm: " + XposedHelpers.getIntField(param.args[2], "hmacAlgorithm"));
                            Log.i(TAG, "CipherSpec - Hmac Enabled: " + XposedHelpers.getBooleanField(param.args[2], "hmacEnabled"));
							// 参数4：Flags，未知标记
                            Log.i(TAG, "Flags: " + param.args[4]);
							// 参数6：加密方式的某个参数PoolSize
                            Log.i(TAG, "PoolSize: " + param.args[6]);
                        }
                    });
        }
    }
}
