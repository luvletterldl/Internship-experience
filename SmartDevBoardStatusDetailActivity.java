package com.ztesoft.manager.ui;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ztesoft.AndrConstants;
import com.ztesoft.eoms.R;
import com.ztesoft.manager.config.DataSource;
import com.ztesoft.manager.config.GlobalVariable;
import com.ztesoft.manager.http.json.IJson;
import com.ztesoft.manager.res.Res;

/**
 * 通用状态分析-详情
 */
public class SmartDevBoardStatusDetailActivity extends ManagerActivity {
	/***************************************/
	private static final int SEARCH_DETAIL = 1;
	private static final int SEARCH_DETAIL_PORT = 11;
	private static final int SEARCH_DETAIL_BGP = 22;
	private static final int SEARCH_DETAIL_TRC = 33; 
	private final int OPER_ORDER_PINNG = 2;

	String tag = "SmartDevBoardStatusDetailActivity";
	private ListView mListView;
	private ListMoreAdapter adapter = null;
	private String ip;
	private String slot;
	private String netKind;
	private String port;
	private String resultStr;
	
	private String areaName;
	private String typeName;
	private String pktSize;
	private String routenum;
	
	
	private List detailList = new ArrayList();
	private List detailTotalList = new ArrayList();
	private Map titleMap = new LinkedHashMap();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 去除title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.smart_devboard_status_oper_detail);
		
		ip = getIntent().getStringExtra("ip");
		slot = getIntent().getStringExtra("slot");
		port = getIntent().getStringExtra("port");
		netKind = getIntent().getStringExtra("netKind");
		
		areaName = getIntent().getStringExtra("areaName");
		typeName = getIntent().getStringExtra("typeName");
		pktSize = getIntent().getStringExtra("pktSize");
		routenum = getIntent().getStringExtra("routenum");

		mListView = (ListView) this.findViewById(R.id.smart_devboard_oper_detail_page_view);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				adapter.notifyDataSetChanged();
			}
		});

		searchDetail();
		
		Button back_home_btn = (Button) findViewById(R.id.back_button);
		back_home_btn.setOnClickListener(this);
		HideSoftInput();
		
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_button:
			this.finish();
			break;
		default:
			break;
		}
	}


	public IJson returnSelf() {
		return (IJson) this;
	}

	@Override
	public boolean parseResponse(int type, String response) {

		Log.i("tag", ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Response "+ type);
		Log.i("tag", ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> response "+ response.toString());

		switch (type) {
		case SEARCH_DETAIL:
		case SEARCH_DETAIL_PORT:
		case SEARCH_DETAIL_BGP:
		case SEARCH_DETAIL_TRC:
			Log.i(tag, "SEARCH_DETAIL:状态分析详情 " + netKind);
			if (response == null || "".equals(response)) {
				removeDialog(DIALOG_PROGRESS);
				new AlertDialog.Builder(this).setTitle("系统提示")
						.setMessage("没有相关信息！")
						.setPositiveButton(Res.UIString.STR_OK, null).show();
				resultStr="null";
			} else {
				parseSmartDataPingDetailJson(response);
				Log.i(tag, netKind + "查询状态分析详情 " + response);
				adapter = new ListMoreAdapter(SmartDevBoardStatusDetailActivity.this);
				mListView.setAdapter(adapter);
				removeDialog(DIALOG_PROGRESS);
				
			}
			
			break;
		case OPER_ORDER_PINNG:
			
			break;
		default:
			break;
		}

		removeDialog(DIALOG_PROGRESS);
		return true;
	}
	
	public void parseSmartDataPingDetailJson(String jsonStr) {
		Log.i(tag, "response--> " + jsonStr);
		removeDialog(DIALOG_PROGRESS);
		try {
			JSONObject content = new JSONObject(jsonStr);
			resultStr=content.optString("OrigInfo");
			String resultCode = content.optString("_flag");
			if ("0".equals(resultCode)) {

				JSONArray titleListNode = content.optJSONArray("titleList");
				// 解析返回的标题列表
				for (int i = 0; i < titleListNode.length(); i++) {
					JSONObject node = (JSONObject) titleListNode.get(i);
					titleMap.put(node.optString("field"),node.optString("field_alise"));
				}
				
				if (titleListNode.length() == 0) {
					//板卡状态
					titleMap.put("Status", "执行状态");
					titleMap.put("OrigInfo", "结果信息");
					titleMap.put("Register", "注册状态");
					titleMap.put("BoardName", "板卡名称");
					titleMap.put("INF_LOG_ID", "INF_LOG_ID");
					titleMap.put("CPUUtiliza", "CPU利用率");
					titleMap.put("MemUsage", "内存使用率");
					titleMap.put("BoardStatus", "板卡状态");
					titleMap.put("Status", "Status");
					//端口状态
					titleMap.put("LastPhyDownTime","LastPhyDownTime");
					titleMap.put("EthPortStat","EthPortStat");
					titleMap.put("TransMode","TransMode");
					titleMap.put("TxPower","TxPower");
					titleMap.put("OutputUtilRate","OutputUtilRate");
					titleMap.put("InputUtilRate","InputUtilRate");
					titleMap.put("TxWarningRange","TxWarningRange");
					titleMap.put("RxWarningRange","RxWarningRange");
					titleMap.put("CRC","CRC");
					titleMap.put("PortBW","PortBW");
					titleMap.put("TransMaxBW","TransMaxBW");
					titleMap.put("RxPower","RxPower");
					titleMap.put("WaveLength","WaveLength");
					titleMap.put("LineProtocolStat","LineProtocolStat");
					titleMap.put("Description","Description");
				}
				Iterator iter = content.keys();
				Map m = null;
				while (iter.hasNext()) {
					m = new HashMap();
					String key = (String) iter.next();
					String value = content.getString(key);
					if(key!=null&&key.equals("Status")){
						if(value!=null&&value.equals("0")){
							//value ="成功";
							value ="IP网管执行成功";
						}else{
							//value = "失败";
							value ="IP网管执行失败";
						}
					}
					m.put(key, value);
					if (titleMap.get(key) != null) {
						detailList.add(m);
					}
					detailTotalList.add(m);
				}
				sortResultList();
			
				
			} else {
				new AlertDialog.Builder(SmartDevBoardStatusDetailActivity.this)
						.setTitle("系统提示").setMessage("调用接口失败")
						.setPositiveButton(Res.UIString.STR_OK, null).show();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			new AlertDialog.Builder(SmartDevBoardStatusDetailActivity.this)
					.setTitle("系统提示").setMessage("解析结果出错!")
					.setPositiveButton(Res.UIString.STR_OK, null).show();
		}
	}

	public class ListMoreAdapter extends BaseAdapter {
		Activity activity;

		public ListMoreAdapter(Activity a) {
			activity = a;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return detailList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			final AppItem appItem;
			if (convertView == null) {
				appItem = new AppItem();
				LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View view;
				view = inflater.inflate(R.layout.smart_qry_detail, parent,false);
				appItem.textName = (TextView) view.findViewById(R.id.text_name);
				appItem.textValue = (TextView) view.findViewById(R.id.text_value);
				appItem.callBtn = (Button) view.findViewById(R.id.take_call);
				view.setTag(appItem);
				convertView = view;
			} else {
				appItem = (AppItem) convertView.getTag();
			}
			Map dataMap = (Map) detailList.get(position);
			Iterator iter = dataMap.keySet().iterator();
			while (iter.hasNext()) {
				String key = (String) iter.next();
				appItem.textValue.setText((String) dataMap.get(key));
				appItem.textName.setText((String) titleMap.get(key) + ":");
				Log.i("titleMap中Key的值", ""+titleMap.get(key));
				//设置让结果信息（原始信息）隐藏
				if(titleMap.get(key)=="结果信息"){
					appItem.textValue.setEllipsize(TruncateAt.END);
					appItem.textValue.setSingleLine(true);
					appItem.textValue.setOnClickListener(new OnClickListener() {
						//控制循环点击作用
						boolean i=true;
						@Override
						public void onClick(View arg0) {
							if(i==true){
								appItem.textValue.setSingleLine(false);
								i=false;
							}else{
								appItem.textValue.setSingleLine(true);
								i=true;
							}
						}
					});
					appItem.callBtn.setVisibility(Button.GONE);
				}else{
					appItem.callBtn.setVisibility(Button.GONE);
				}
				appItem.textName.setTextSize(SetPx(15));
				appItem.textValue.setTextSize(SetPx(15));
				appItem.textName.setGravity(Gravity.CENTER_VERTICAL);
				appItem.textValue.setGravity(Gravity.CENTER_VERTICAL);
			}

			return convertView;
		}

	}

	/**
	 * 隐藏软键盘
	 * */
	public void HideSoftInput() {
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		View view = this.getCurrentFocus();
		if (view != null) {
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);// 隐藏软键盘
		}
	}

	public void searchDetail() {

		showProgress(null, "查询中，请稍后...", null, null, false);
		if ("DevBoardStatus".equals(netKind)) {
			sendRequest(this, SEARCH_DETAIL, 0, netKind);
		} else if ("PortStatusAnalyse".equals(netKind)) {
			sendRequest(this, SEARCH_DETAIL_PORT, 0, netKind);
		} else if ("BGPAnalyse".equals(netKind)) {
			sendRequest(this, SEARCH_DETAIL_BGP, 0, netKind);
		} else if ("TraceRouterCheck".equals(netKind)) {
			sendRequest(this, SEARCH_DETAIL_TRC, 0, netKind);
		}
		
	}

	// 调用服务器的方法地址
	@Override
	public String getRequestContent(int type) {
			return DataSource.getInstance().getSysAdress()+ GlobalVariable.CLIENT_LINK_NEW; // 通用解析地址;
		
	}

	// 调用服务器的方法报文
	@Override
	public Map getRequestData(int type, String nextTeachName) {
		Map params = new HashMap();
		try {
			switch (type) {			
			case SEARCH_DETAIL:
				JSONObject jsonData = new JSONObject();				
				jsonData.put("actionName", "InfCommon");
				jsonData.put("operationCode", "smartDevBoardOperForEBiz");
				jsonData.put("ip", ip);
				jsonData.put("slot", slot);
				params.put("params", jsonData.toString());
				break;
			case SEARCH_DETAIL_PORT:
				JSONObject jsonData2 = new JSONObject();				
				jsonData2.put("actionName", "InfCommon");
				jsonData2.put("operationCode", "smartPortStatusOperForEBiz");
				jsonData2.put("ip", ip);
				jsonData2.put("slot", slot);
				jsonData2.put("port", port);
				params.put("params", jsonData2.toString());
				break;
			case SEARCH_DETAIL_BGP:
				JSONObject jsonData3 = new JSONObject();				
				jsonData3.put("actionName", "InfCommon");
				jsonData3.put("operationCode", "smartBGPOperForEBiz");
				jsonData3.put("ip", ip);
				params.put("params", jsonData3.toString());
				break;
			case SEARCH_DETAIL_TRC:
				JSONObject jsonData4 = new JSONObject();				
				jsonData4.put("actionName", "InfCommon");
				jsonData4.put("operationCode", "smartTraceRouterOperForEBiz");
				jsonData4.put("ip", ip);
				jsonData4.put("areaName", areaName);
				jsonData4.put("typeName", typeName);
				jsonData4.put("pktSize", pktSize);
				jsonData4.put("routenum", routenum);
				params.put("params", jsonData4.toString());
				break;
			case OPER_ORDER_PINNG:
				
				break;
			default:
				return null;
			}  
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			Log.i(tag, e.getMessage());
		}
		return params;
	}


	public void sortResultList() {
		Iterator iter = titleMap.keySet().iterator();
		List newList = new ArrayList();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			for (int i = 0; i < detailList.size(); i++) {
				Map m = (Map) detailList.get(i);
				Iterator mIter = m.keySet().iterator();
				while (mIter.hasNext()) {
					String mkey = (String) mIter.next();
					if (mkey.equals(key)) {
						newList.add(m);
						break;
					}
				}
			}
		}
		detailList = newList;
	}

	private void setResultCode(int resultCode, Intent intent) {
		//
		Log.i("ADI调试信息", "返回操作码" + resultCode);
		// intent = this.getIntent();
		intent.putExtra("RESULT_CODE", resultCode);
		setResult(resultCode, intent);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i("调用函数...", "onActivityResult被调用了...");
		removeDialog(DIALOG_PROGRESS);
		super.onActivityResult(requestCode, resultCode, data);
		if (555 == requestCode) {

			if (AndrConstants.RESULT_CODE.RESULT_OK == resultCode) {
				Log.i("ADI调试信息", "需要刷新数据");
				this.setResult(AndrConstants.RESULT_CODE.RESULT_OK);
				this.finish();
			}
		}
		if (333 == requestCode) {

			if (AndrConstants.RESULT_CODE.RESULT_OK == resultCode) {
				this.setResult(AndrConstants.RESULT_CODE.RESULT_OK);
				// this.finish();
			}
		}
	}

	class AppItem {
		TextView textName;
		TextView textValue;
		Button callBtn;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
