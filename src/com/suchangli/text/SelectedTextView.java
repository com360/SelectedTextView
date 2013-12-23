package com.suchangli.text;

import android.content.Context;
import android.text.Layout;
import android.text.Selection;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.Toast;

public class SelectedTextView extends EditText{
	private boolean DEBUG = true;
	private static String TAG = "MyTextView";

	public SelectedTextView(Context context){
		super(context);
		initialize();
	}
	public SelectedTextView(Context context, AttributeSet attrs){
		super(context, attrs);
		initialize();
	}
	private void initialize(){
		setGravity(Gravity.TOP);
	}

	
	@Override
	public boolean getDefaultEditable() {
		return false;
	}
 
	private long mStartTime = 0;
	private boolean mLongClick = false;
	private String mWord = null;
	private int[] mWordScop = new int[]{-1,-1};
	public boolean onTouchEvent(MotionEvent event) {
		 
		switch (event.getAction()){
			case MotionEvent.ACTION_DOWN:
				mStartTime = System.currentTimeMillis();
				break;
			case MotionEvent.ACTION_MOVE:
				if(mLongClick){
					String retWord = getTargetWord(event);
					wordPreviewWindow(retWord);
				}else{
					if(System.currentTimeMillis()-mStartTime > 500){
						mLongClick = true;
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				
				if(mLongClick){
					//显示词典对话框
					showDictDialog(mWord);
					mLongClick = false;
					Selection.removeSelection(getEditableText());
				}
				mStartTime = 0;
				break;
		}
		
		return true;
	}
	/**
	 * 弹出单词查询的对话框
	 * 在这个过程中需要通过词典接口进行词典查询
	 * @param word
	 */
	protected void showDictDialog(String word){
		if(!TextUtils.isEmpty(word)){
			Toast.makeText(getContext(), word, 1).show();
		}
		
	}
	/**
	 * 显示单词的预览，这里可以用放大镜的效果去处理
	 * 接下来会做这里的实现
	 * @param word
	 */
	protected void wordPreviewWindow(String word){
		if(DEBUG){
			Log.d(TAG, "点击的单词："+ word );
		}
	}
	//单词结束字符，有可能不全，需要添加，为了执行效率可以查ASCCII码
	private static char[] sStopCharArray = new char[]{' ',',','.',';','?','!','(',')'};
	//检查单词是否结束，结束返回true，否则返回false,这里这种检测的方式效率不高，接下来会对这个地方进行优化
	private boolean checkStoped(char c){
		for(char cr : sStopCharArray){
			if(cr-c==0){
				return true;
			}
		}
		
		return false;
	} 
	/**
	 * 这个方法就是为了确定点击的是哪一个单词
	 * 问题：在我刷的android4.0上，无法显示选择效果，以后会继续测试其他手机，如果有主流手机无法显示，会采用
	 * 另一种方式进行处理选择的问题
	 * @param event
	 * @return 点击的单词，如果为空就返回空字符串
	 */
	private String getTargetWord(MotionEvent event){
		
		
		String text = getText().toString();
		
		Layout layout = getLayout();
		//获得点击的行
		int lineNum = layout.getLineForVertical(getScrollY() + (int) event.getY());
		//获得点击的字符位置，所点击的这一行的位置
		int lineoff = layout.getOffsetForHorizontal(lineNum,(int) event.getX());
		//点击到了字符外面就不用计算了，直接返回空字符串
		if(lineoff == text.length()){
			Selection.removeSelection(getEditableText());
			if(DEBUG){
				Log.d(TAG, "点击的行号：" + lineNum + ",点击所在行的字符位置:" + lineoff);
			}
			mWord = "";
			return mWord;
		}
		//如果是同一个单词，防止重复计算
		if(mLongClick&&mWordScop[0] <= lineoff && lineoff < mWordScop[1]){
			
			return mWord;
		}
		
		
		Selection.removeSelection(getEditableText());
		if(DEBUG){
			Log.d(TAG, "点击的行号：" + lineNum + ",点击所在行的字符位置:" + lineoff);
			Log.d(TAG, "对应的字符:"+text.charAt(lineoff));
			if(lineoff>1||text.length()>lineoff-1){
				Log.d(TAG, "对应的字符:"+text.charAt(lineoff-1)+text.charAt(lineoff)+text.charAt(lineoff+1));
			}
		}
		 
		//获得点击的所在行最后一个字符的位置,所有字符的位置
		int lineEndCharPostion = layout.getLineEnd(lineNum);
		//获得点击的所在行第一个字符的位置，所有字符的位置
		int lineStartCharPostion = layout.getLineStart(lineNum);
		
		if(DEBUG){
			Log.d(TAG, "lineEndCharPostion：" + lineEndCharPostion + ",lineStartCharPostion:" + lineStartCharPostion);
		}
		
		//计算点击的单词的在textview中的位置
		int clikeCharPostion = lineoff;
		
		int wordStartPosition = -1;
		
		if(DEBUG){
			
			Log.d(TAG,"clikeCharPostion:"+clikeCharPostion+ ",lineEndCharPostion：" + lineEndCharPostion + ",lineStartCharPostion:" + lineStartCharPostion);
		}
		
		if(checkStoped(text.charAt(clikeCharPostion))){
			return "";
		}
		//左移动
		for(int i = clikeCharPostion; i >= 0; i--){
			if(i==0){
				wordStartPosition = i;
			}
			
			char c = text.charAt(i);
			if(checkStoped(c)){
				wordStartPosition = i+1;
				break;
			}
			
		}
		//右移动
		int wordEndPosition = -1;
		for(int i = clikeCharPostion; i < text.length(); i++){
			if(i == text.length()-1){
				wordEndPosition = i+1;
			}
			char c = text.charAt(i);
			if(checkStoped(c)){
				wordEndPosition = i;
				break;
			}
		}
		if(DEBUG){
			Log.d(TAG, "wordStartPosition：" + wordStartPosition + ",wordEndPosition:" + wordEndPosition);
		}
		//word
		if(wordStartPosition<0 || wordEndPosition > text.length()){
			return "";
		}
		String word = text.substring(wordStartPosition, wordEndPosition);
		mWord = word;
		
		
		if(DEBUG){
			Log.d(TAG, "word：" + mWord);
		}
		
		//保存单词的范围
		mWordScop[0] = wordStartPosition;
		mWordScop[1] = wordEndPosition;
		
		if(wordStartPosition > 0 && wordEndPosition > 0){
			Selection.setSelection(getEditableText(), wordStartPosition, wordEndPosition);
		}
		
		return word;
	}
	/**
	 * 半角转全角字符，这个方法用了处理英文排版不好看的，但是会增加内存（半角是占一个字节，全角占两个自己）
	 * @param input
	 * @return
	 */
	public static String toDBC(String input) {
		   char[] c = input.toCharArray();
		   for (int i = 0; i< c.length; i++) {
		       if (c[i] == 12288) {
		         c[i] = (char) 32;
		         continue;
		       }if (c[i]> 65280&& c[i]< 65375)
		          c[i] = (char) (c[i] - 65248);
		       }
		   return new String(c);
	}
}
