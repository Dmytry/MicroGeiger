package com.example.microgeiger;

import android.app.Application;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MicroGeigerApp extends Application {
	private static final String TAG = "MicroGeiger";
	public volatile int total_count=0;
	public final int sample_rate=44100;
	public final int counters_update_rate=1;/// sample rate must be divisible by counters update rate
	public final int samples_per_update=44100;
	public class Counter{
		public int counts[];
		public int pos=0;		
		volatile public int count=0;
		public double scale=1.0;
		public String name;
		Counter(int n_counts, double scale_, String name_){
			counts=new int[n_counts];
			scale=scale_;
			name=name_;
		}
		public void push(int n){			
			pos=pos+1;
			if(pos>=counts.length)pos=0;
			count-=counts[pos];
			counts[pos]=n;
			count+=n;
		}
		double getValue(){
			return scale*count;
		}
	}
	volatile Counter counters[];
  
    private class Listener implements Runnable{
    	public volatile boolean do_stop=false;
		@Override
		public void run() {	
			AudioRecord recorder;			
			final int dead_time=sample_rate/2000;
			final double threshold=0.1;
			double running_avg=0.0;
			double running_avg_const=0.0001;
			int dead_counter=0;
			int sample_update_counter=0;
			int sample_count=0;
			int data_size=Math.max(sample_rate/5, AudioRecord.getMinBufferSize(sample_rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT));
			short data[]=new short[data_size];
			recorder=new AudioRecord (AudioSource.MIC, sample_rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, data_size);
			try{
			    while(!do_stop){			    	
			    	if (recorder.getState()==android.media.AudioRecord.STATE_INITIALIZED){ // check to see if the recorder has initialized yet.
			            if (recorder.getRecordingState()==android.media.AudioRecord.RECORDSTATE_STOPPED){
			                 recorder.startRecording();
			            }else{
			            	int read_size=recorder.read(data,0,data_size);
			            	for(int i=0; i<read_size; ++i){
			            		if(dead_counter>0){
			            			dead_counter--;			            			
			            		}
			            		sample_update_counter++;
			            		if(sample_update_counter>=samples_per_update){
			            			for(int j=0;j<counters.length;++j){
			            				counters[j].push(sample_count);			            				
			            			}
			            			sample_update_counter=0;
			            			sample_count=0;
			            			//Log.d(TAG, "got a sample");
			            		}
			            		double raw_v=data[i]*(1.0/32768.0);
			            		running_avg=running_avg*(1.0-running_avg_const)+raw_v*running_avg_const;
			            		double v=raw_v-running_avg;
			            		if(v>threshold){
			            			if(dead_counter<=0){
			            				total_count++;
			            				sample_count++;
			            				dead_counter=dead_time;
			            				
			            			}		            			
			            		}
			            	}			            	
				    	}
			    	}else{
				    	Log.d(TAG, "failed to initialize audio");
				    	Thread.sleep(5000);
				    }
			    }
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    finally{
		    	recorder.stop();
		    	recorder.release();
		    }
		}
	}
    
    static Listener listener;
    static Thread listener_thread;
    
    void start(){
    	counters=new Counter[4];
    	counters[0]=new Counter(counters_update_rate*5, 12.0, " CPM in last 5 sec");
    	counters[1]=new Counter(counters_update_rate*30, 2.0, " CPM in last 30 sec");
    	counters[2]=new Counter(counters_update_rate*120, 0.5, " CPM in last 2 min");
    	counters[3]=new Counter(counters_update_rate*600, 0.1, " CPM in last 10 min");
    	if(listener_thread==null){
	        listener=new Listener();
	        listener_thread=new Thread(listener);
	        listener_thread.start();
        }
    }
    void stop(){
    	if(listener!=null){
    		listener.do_stop=true;
    		listener_thread.interrupt();
    	}    
    }
    @Override
	public void onCreate() {    	
        super.onCreate();   
        start();
    }
    @Override
    public void onTerminate() {    	
        super.onTerminate();
        stop();
    }
}
