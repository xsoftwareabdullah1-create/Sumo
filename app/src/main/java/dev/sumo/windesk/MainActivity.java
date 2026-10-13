package dev.sumo.windesk;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
 private final int bg=Color.rgb(7,12,25), panel=Color.rgb(17,29,49), cyan=Color.rgb(64,220,255);
 private FrameLayout desktop; private View pointer; private TextView selectedFile; private Uri selectedUri;
 @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().setStatusBarColor(bg);getWindow().setNavigationBarColor(bg);getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);buildDesktop();}
 private void buildDesktop(){
  desktop=new FrameLayout(this); desktop.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{Color.rgb(7,15,39),Color.rgb(12,42,72),Color.rgb(6,12,26)}));
  LinearLayout content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(dp(24),dp(16),dp(24),dp(16));desktop.addView(content,new FrameLayout.LayoutParams(-1,-1));
  TextView bar=label("SUMO  /  WINDESK                                      ANDROID WINDOWS WORKSPACE",12,cyan,true);content.addView(bar);
  TextView title=label("Windows Workstation",30,Color.WHITE,true);title.setPadding(0,dp(12),0,dp(4));content.addView(title);
  content.addView(label("Landscape desktop • touch/mouse pointer • EXE runtime delegated to Winlator",13,Color.rgb(177,202,225),false));
  LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.setPadding(0,dp(18),0,dp(14));content.addView(row,new LinearLayout.LayoutParams(-1,0,1f));
  row.addView(card("EXE RUNNER","Choose a Windows EXE, then open Winlator to run it in a Wine container.","SELECT .EXE",v->pickExe()),new LinearLayout.LayoutParams(0,-1,1));
  row.addView(new View(this),new LinearLayout.LayoutParams(dp(14),1));
  row.addView(card("CODEX CLI","Open Termux where Codex CLI can use your existing login.","OPEN TERMUX",v->launchPackage("com.termux","Install Termux first.")),new LinearLayout.LayoutParams(0,-1,1));
  row.addView(new View(this),new LinearLayout.LayoutParams(dp(14),1));
  row.addView(card("WINDOWS RUNTIME","Winlator supplies Wine and CPU translation; install it separately.","OPEN WINLATOR",v->launchPackage("com.winlator","Install a compatible Winlator build first.")),new LinearLayout.LayoutParams(0,-1,1));
  selectedFile=label("No EXE selected. Choose a trusted .exe file to begin.",12,Color.WHITE,false);selectedFile.setPadding(dp(14),dp(12),dp(14),dp(12));selectedFile.setBackground(round(panel,dp(10),Color.rgb(38,69,96)));content.addView(selectedFile,new LinearLayout.LayoutParams(-1,-2));
  TextView foot=label("This launcher does not contain Wine/Box64. EXE compatibility depends on Winlator and your device.",11,Color.rgb(143,174,202),false);foot.setPadding(0,dp(10),0,0);content.addView(foot);
  pointer=new View(this);pointer.setBackground(round(cyan,dp(3),Color.WHITE));FrameLayout.LayoutParams pp=new FrameLayout.LayoutParams(dp(10),dp(10));pp.leftMargin=dp(24);pp.topMargin=dp(74);desktop.addView(pointer,pp);
  desktop.setOnTouchListener((v,e)->{if(pointer!=null&&(e.getAction()==MotionEvent.ACTION_MOVE||e.getAction()==MotionEvent.ACTION_DOWN)){FrameLayout.LayoutParams p=(FrameLayout.LayoutParams)pointer.getLayoutParams();p.leftMargin=Math.max(0,(int)e.getX());p.topMargin=Math.max(0,(int)e.getY());pointer.setLayoutParams(p);}return false;});
  setContentView(desktop);
 }
 private TextView label(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextColor(color);t.setTextSize(size);if(bold)t.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);return t;}
 private View card(String head,String body,String action,View.OnClickListener listener){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(16),dp(16),dp(16),dp(16));c.setBackground(round(panel,dp(14),Color.rgb(43,72,101)));c.addView(label(head,12,cyan,true));TextView b=label(body,14,Color.WHITE,false);b.setPadding(0,dp(12),0,dp(16));c.addView(b,new LinearLayout.LayoutParams(-1,0,1));Button btn=new Button(this);btn.setText(action);btn.setAllCaps(false);btn.setTextColor(Color.BLACK);btn.setBackground(round(cyan,dp(10),cyan));btn.setOnClickListener(listener);c.addView(btn,new LinearLayout.LayoutParams(-1,dp(46)));return c;}
 private void pickExe(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");try{startActivityForResult(i,42);}catch(ActivityNotFoundException e){toast("No file picker available.");}}
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request!=42||result!=RESULT_OK||data==null||data.getData()==null)return;selectedUri=data.getData();try{getContentResolver().takePersistableUriPermission(selectedUri,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}selectedFile.setText("Selected: "+selectedUri.getLastPathSegment()+"\nOpen Winlator and locate this file using its file manager/shared storage. Android document URIs are not a universal Winlator launch contract.");toast("File selected. Open Winlator to run it in a container.");}
 private void launchPackage(String pkg,String error){Intent i=getPackageManager().getLaunchIntentForPackage(pkg);if(i==null){toast(error);return;}try{startActivity(i);}catch(Exception e){toast("Could not launch app.");}}
 private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
 private GradientDrawable round(int color,int radius,int stroke){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);d.setStroke(dp(1),stroke);return d;}
 private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
}