package com.example.microgeiger;

import java.text.DecimalFormat;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.widget.TextView;

public class MainActivity extends Activity {
	Handler handler;
	boolean stop_handler=false;
	int old_count=-1;
	Panel panel;
	MicroGeigerApp app;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
        panel=new Panel(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //setContentView(R.layout.activity_main);        
        setContentView(panel);
        handler=new Handler();     
                        
        app=(MicroGeigerApp) getApplication();
        
        Runnable runnable = new Runnable() {
            @Override
            public void run() {            	
                if (!stop_handler) {
                	/*
                	if(old_count!=app.total_count){
	            		//TextView text = (TextView) findViewById(R.id.count_total);
        				//text.setText(Integer.toString(app.count));
                		panel.invalidate();
	            	}*/
                	panel.invalidate();
                    handler.postDelayed(this, 500);
                }
            }
        };
        // start it with:
        handler.post(runnable);
        
    }
    @Override
    protected void onDestroy() {
    	stop_handler=true;
        super.onDestroy();        
    }
    
    class Panel extends android.view.View {
    	public Panel(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		public void RedrawControl(android.graphics.Canvas canvas){
			if(app==null)return;
    		Paint p=new Paint();
    		p.setColor(Color.WHITE); 
    		p.setTextSize(30); 
    		int ypos=50;
    		int ydelta=60;
    		canvas.drawText(Integer.toString(app.total_count)+" total", 10, ypos, p);
    		ypos+=ydelta;
    		DecimalFormat decim = new DecimalFormat("0.0");
    		for(int i=0;i<app.counters.length;++i){
    			canvas.drawText(decim.format(app.counters[i].getValue())+" "+app.counters[i].name, 10, ypos, p);
    			ypos+=ydelta;
    		}
    		
    		/*
    		float y0=canvas.getHeight()-300;
    		float y1=canvas.getHeight()-100;
    		float w=130;
    		float margin=2;
    		p.setColor(motor1?Color.WHITE:Color.BLACK);
    		canvas.drawRect(canvas.getWidth()-w+margin,y0,canvas.getWidth(),y1,p);
    		p.setColor(motor2?Color.WHITE:Color.BLACK);
    		canvas.drawRect(canvas.getWidth()-2*w+margin,y0,canvas.getWidth()-w,y1,p);
    		*/
    	}

		@Override
		public void onDraw(android.graphics.Canvas canvas){
			canvas.drawColor(Color.BLACK);
			//Paint my_paint=new Paint();
			//my_paint.setColor(Color.RED);
			//canvas.drawRect(0,0,100,100,my_paint);
			RedrawControl(canvas);
		}
    }
    
}