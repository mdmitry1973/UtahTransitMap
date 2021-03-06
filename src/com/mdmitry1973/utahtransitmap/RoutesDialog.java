package com.mdmitry1973.utahtransitmap;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class RoutesDialog extends Dialog {
	
	public class TimeTableItem implements Serializable {
		private String strTime = "";
		private Boolean bChecked = false;
		private String strTripId = "";

		public TimeTableItem(String strTime, Boolean bChecked, String strTripId) {
			this.strTime = strTime;
			this.bChecked = bChecked;
			this.strTripId = strTripId;
		}
	
		public String getTime() {
			
			return strTime;
		}
		
		public String getTripId() {
			
			return strTripId;
		}
	}
	
	private	MainActivity m_activity = null;
	private	String m_stop_id = null;
	private Map<String, ArrayList<String>> mapServiceData = null;
	private Map<Button, String> mapStopButtom = new HashMap<Button, String>();
	private File stopFile = null;
	
	private Map<Button, String> mapButtonDayTime = new HashMap<Button, String>();
	private Map<String, ArrayList<TimeTableItem>> mapDayTime = new HashMap<String, ArrayList<TimeTableItem>>();
	private ArrayList<Button> ButtonList = new ArrayList<Button>();
	private ListView listView = null;
	
	public static String cur_tripsId = "";
	
	String route_id = "";
	
	public class TimeTableListAdapter extends ArrayAdapter<TimeTableItem> {

		private List<TimeTableItem> items;
		private ArrayList<AtomPaymentHolder> holders = new ArrayList<AtomPaymentHolder>();
		private int layoutResourceId;
		private Context context;
		
		public class AtomPaymentHolder {
			TimeTableItem atomPayment;
			TextView strTime;
			Button checkedButton;
		}

		public TimeTableListAdapter(Context context, int layoutResourceId, List<TimeTableItem> items) {
			super(context, layoutResourceId, items);
			this.layoutResourceId = layoutResourceId;
			this.context = context;
			this.items = items;
		}
		
		 void resetArray(List<TimeTableItem> objects)
		    {
			 	holders.clear();
		    	clear();
		    	items.clear();
		    	
		    	for (int i = 0; i < objects.size(); ++i) 
		    	{
		    		items.add(objects.get(i));
				}
		    }

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			AtomPaymentHolder holder;
            if(convertView == null){
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.time_table_item, parent, false);
                holder = new AtomPaymentHolder();

                holder.strTime = (TextView)convertView.findViewById(R.id.textViewTimeItem);
                holder.checkedButton = (Button)convertView.findViewById(R.id.buttonShowRoute);

                convertView.setTag(holder);
                
                holders.add(holder);
            }else{
                holder = (AtomPaymentHolder) convertView.getTag();
            }
            
            String strTripId = getItem(position).getTripId();
            
            if (cur_tripsId.compareTo(strTripId) == 0)
            {
            	holder.checkedButton.setCompoundDrawablesWithIntrinsicBounds(null,null,null,  m_activity.getResources().getDrawable( R.drawable.on));
            }
            else
            {
            	holder.checkedButton.setCompoundDrawablesWithIntrinsicBounds(null,null,null,  m_activity.getResources().getDrawable( R.drawable.off));
            }

            holder.strTime.setText(getItem(position).getTime());
            holder.checkedButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) 
                {
                	Log.v("MainActivity", "setOnClickListener= " + v.getTag());
                	
                	if (m_activity != null)
                	{
                		m_activity.setTripOverlay((String) v.getTag(), route_id);
                		
                		for(int i = 0; i < holders.size(); i++)
            			{
            				AtomPaymentHolder h = holders.get(i);
            			
            				h.checkedButton.setCompoundDrawablesWithIntrinsicBounds(null,null,null,  m_activity.getResources().getDrawable( R.drawable.off));
            			}
                		
                		((Button)v).setCompoundDrawablesWithIntrinsicBounds(null,null,null,  m_activity.getResources().getDrawable( R.drawable.on));
                	
                		cur_tripsId = (String) v.getTag();
                	}
                }
            });
            holder.checkedButton.setTag(strTripId);
            return convertView;
		}
	}
	
	TimeTableListAdapter adapter = null;

	
	public RoutesDialog(Context context, String stop_id) {
		super(context);
		
		m_activity = (MainActivity)context;
		m_stop_id = stop_id;
		
		mapServiceData = m_activity.getMpServiceData();
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.stop_dialog);
		setCanceledOnTouchOutside(true);
		
		File externalCacheDir = context.getExternalCacheDir();
		stopFile = new File(new File(externalCacheDir, "stops_data"), String.format("%s", stop_id));
		
		TextView nameItem = (TextView) findViewById(R.id.textViewStopName);
		TextView descItem = (TextView) findViewById(R.id.textViewStopDesc);
		TextView codeItem = (TextView) findViewById(R.id.textViewStopCode);
		
		LinearLayout layoutItem = (LinearLayout) findViewById(R.id.routes);
		
		try {
			DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder xmlBuilder;
			xmlBuilder = xmlFactory.newDocumentBuilder();
			Document document;
			document = xmlBuilder.parse(stopFile);
			Element elRoot = document.getDocumentElement();
			
			/*
		  	<?xml version="1.0" encoding="UTF-8" standalone="no"?>
			<Stop stop_code="SOTNMALL" stop_desc="UNNAMED ST" stop_id="100" stop_lat="40.564691" stop_lon="-111.895258" stop_name="SOUTH TOWNE MALL">
			  <routesItems>
			    <Route roudeId="41266" route_long_name="DRAPER FLEX" route_short_name="F546">
			      <Trip arrival_time="8:40:00" block_id="723945" departure_time="8:40:00" direction_id="1" service_id="4" shape_id="92527" stop_headsign="" stop_id="100" stop_sequence="2" trip_headsign="F546 DRAPER FLEX - TO DRAPER" trip_id="1515712"/>
			    </Route>
			  </routesItems>
			</Stop>
		 */
			
			String stop_code = elRoot.getAttribute("stop_code");
			String stop_desc = elRoot.getAttribute("stop_desc");
			String stop_name = elRoot.getAttribute("stop_name");
			
			nameItem.setText(stop_name);
			descItem.setText(stop_desc);
			codeItem.setText(stop_code);
			
			NodeList roudeNodes = elRoot.getElementsByTagName("routesItems");
			
			if (roudeNodes.getLength() > 0)
			{
				Element routeNode = (Element)roudeNodes.item(0);
				
				NodeList roudeList = routeNode.getElementsByTagName("Route");
				
				for(int ii = 0; ii < roudeList.getLength(); ii++)
				{
					routeNode = (Element)roudeList.item(ii);
					
					String str_roudeId = routeNode.getAttribute("roudeId");
					String str_route_long_name = routeNode.getAttribute("route_long_name");
					String str_route_short_name = routeNode.getAttribute("route_short_name");
					
					Button routeButton = new Button(getContext());
					
					routeButton.setText(str_route_short_name + "\n" + str_route_long_name);
					
					mapStopButtom.put(routeButton, str_roudeId);
					
					routeButton.setOnClickListener(new View.OnClickListener() {
				        @Override
				        public void onClick(View v) {
				          
				        	Log.v("MainActivity", "Clicked button");
				        	
				        	route_id = mapStopButtom.get(v);
				        	
				        	cur_tripsId = m_activity.getCurrentTripOverlay(route_id);
				        	
				        	final Dialog dialog = new Dialog(m_activity);
							
							dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
							dialog.setContentView(R.layout.route_time_table);
							
							dialog.setCanceledOnTouchOutside(true);
							
							int currentDay = m_activity.getCurrentDay();
							
							{
								try {
									DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
									DocumentBuilder xmlBuilder;
									xmlBuilder = xmlFactory.newDocumentBuilder();
									Document document;
									document = xmlBuilder.parse(stopFile);
									Element elRoot = document.getDocumentElement();
									
									/*
								  	<?xml version="1.0" encoding="UTF-8" standalone="no"?>
									<Stop stop_code="SOTNMALL" stop_desc="UNNAMED ST" stop_id="100" stop_lat="40.564691" stop_lon="-111.895258" stop_name="SOUTH TOWNE MALL">
									  <routesItems>
									    <Route roudeId="41266" route_long_name="DRAPER FLEX" route_short_name="F546">
									      <Trip arrival_time="8:40:00" block_id="723945" departure_time="8:40:00" direction_id="1" service_id="4" shape_id="92527" stop_headsign="" stop_id="100" stop_sequence="2" trip_headsign="F546 DRAPER FLEX - TO DRAPER" trip_id="1515712"/>
									    </Route>
									  </routesItems>
									</Stop>
								 */
									
									
									NodeList roudeNodes = elRoot.getElementsByTagName("routesItems");
									
									if (roudeNodes.getLength() > 0)
									{
										Element routeNode = (Element)roudeNodes.item(0);
										
										NodeList roudeList = routeNode.getElementsByTagName("Route");
										
										for(int ii = 0; ii < roudeList.getLength(); ii++)
										{
											routeNode = (Element)roudeList.item(ii);
											
											String str_roudeId = routeNode.getAttribute("roudeId");
											//String str_route_long_name = routeNode.getAttribute("route_long_name");
											//String str_route_short_name = routeNode.getAttribute("route_short_name");
											
											if (route_id.compareTo(str_roudeId) == 0)
											{
												NodeList trips = routeNode.getChildNodes();
												
												ArrayList<TimeTableItem> tagMoData = new ArrayList<TimeTableItem>();
												ArrayList<TimeTableItem> tagTuData = new ArrayList<TimeTableItem>();
												ArrayList<TimeTableItem> tagWeData = new ArrayList<TimeTableItem>();
												ArrayList<TimeTableItem> tagThData = new ArrayList<TimeTableItem>();
												ArrayList<TimeTableItem> tagFrData = new ArrayList<TimeTableItem>();
												ArrayList<TimeTableItem> tagSaData = new ArrayList<TimeTableItem>();
												ArrayList<TimeTableItem> tagSuData = new ArrayList<TimeTableItem>();
												
												for(int m = 0; m < trips.getLength(); m++)
												{
													Element tripEl = (Element)trips.item(m);
													String arrival_time = tripEl.getAttribute("arrival_time");
													String service_id = tripEl.getAttribute("service_id");
													String trip_id = tripEl.getAttribute("trip_id");
													ArrayList<String> arrayList = mapServiceData.get(service_id);
													TimeTableItem itemTime = new TimeTableItem(arrival_time, false, trip_id);
													
													if (arrayList.get(0).compareTo("1") == 0)
													{
														tagMoData.add(itemTime);
													}
													
													if (arrayList.get(1).compareTo("1") == 0)
													{
														tagTuData.add(itemTime);
													}
													
													if (arrayList.get(2).compareTo("1") == 0)
													{
														tagWeData.add(itemTime);
													}
													
													if (arrayList.get(3).compareTo("1") == 0)
													{
														tagThData.add(itemTime);
													}
													
													if (arrayList.get(4).compareTo("1") == 0)
													{
														tagFrData.add(itemTime);
													}
													
													if (arrayList.get(5).compareTo("1") == 0)
													{
														tagSaData.add(itemTime);
													}
													
													if (arrayList.get(6).compareTo("1") == 0)
													{
														tagSuData.add(itemTime);
													}
												}
												
												mapDayTime.clear();
												mapDayTime.put("Mo", tagMoData);
												mapDayTime.put("Tu", tagTuData);
												mapDayTime.put("We", tagWeData);
												mapDayTime.put("Th", tagThData);
												mapDayTime.put("Fr", tagFrData);
												mapDayTime.put("Sa", tagSaData);
												mapDayTime.put("Su", tagSuData);
												
												Collection<ArrayList<TimeTableItem>> col = mapDayTime.values();
												Iterator<ArrayList<TimeTableItem>> it = col.iterator();
												ArrayList<TimeTableItem> tagXXData = null;
												
												do
												{
													tagXXData = it.next();
													
													if (tagXXData != null)
													{
														Collections.sort(tagXXData, new Comparator<TimeTableItem> () 
																{
																	@Override
																	public int compare(TimeTableItem lhs,
																			TimeTableItem rhs) 
																	{
																		String[] lhsArr = lhs.strTime.split(":");
																		String[] rhsArr = rhs.strTime.split(":");
																		
																		Time a = new Time();
																		Time b = new Time();
																		
																		 a.set(Integer.parseInt(lhsArr[2]), Integer.parseInt(lhsArr[1]), Integer.parseInt(lhsArr[0]), 0, 0, 0);
																		 b.set(Integer.parseInt(rhsArr[2]), Integer.parseInt(rhsArr[1]), Integer.parseInt(rhsArr[0]), 0, 0, 0);
																    	
																		 return Time.compare(a, b);
																	}
																});
													}
												}while(tagXXData != null);
												
												break;
											}
										}
									}
								}
								catch(Exception ex)
								{
									Log.v("MainActivity", "trouble ", ex);
								}
							}
							
							listView = (ListView) dialog.findViewById(R.id.listViewTrips);
							
							Button ButtonMo = (Button) dialog.findViewById(R.id.toggleButtonMo);
							Button ButtonTu = (Button) dialog.findViewById(R.id.toggleButtonTu);
							Button ButtonWe = (Button) dialog.findViewById(R.id.toggleButtonWe);
							Button ButtonTh = (Button) dialog.findViewById(R.id.toggleButtonTh);
							Button ButtonFr = (Button) dialog.findViewById(R.id.toggleButtonFr);
							Button ButtonSa = (Button) dialog.findViewById(R.id.toggleButtonSa);
							Button ButtonSu = (Button) dialog.findViewById(R.id.toggleButtonSu);
							
							mapButtonDayTime.clear();
							mapButtonDayTime.put(ButtonMo, "Mo");
							mapButtonDayTime.put(ButtonTu, "Tu");
							mapButtonDayTime.put(ButtonWe, "We");
							mapButtonDayTime.put(ButtonTh, "Th");
							mapButtonDayTime.put(ButtonFr, "Fr");
							mapButtonDayTime.put(ButtonSa, "Sa");
							mapButtonDayTime.put(ButtonSu, "Su");
							
							ButtonList.clear();
							ButtonList.add(ButtonMo);
							ButtonList.add(ButtonTu);
							ButtonList.add(ButtonWe);
							ButtonList.add(ButtonTh);
							ButtonList.add(ButtonFr);
							ButtonList.add(ButtonSa);
							ButtonList.add(ButtonSu);
							
							for(int nn = 0; nn < ButtonList.size(); nn++)
							{
								Button bt = ButtonList.get(nn);
								
								bt.setOnClickListener(new View.OnClickListener() {
							        @Override
							        public void onClick(View v) {
							           
							        	Log.v("MainActivity", "Clicked ButtonMo");
							        	
							        	String dayName = mapButtonDayTime.get(v);
							        	ArrayList<TimeTableItem> dayTime = mapDayTime.get(dayName);
							        	
							        	for(int k = 0; k < ButtonList.size(); k++)
							        	{
							        		if (v.equals(ButtonList.get(k)) == true)
							        		{
							        			ButtonList.get(k).setCompoundDrawablesWithIntrinsicBounds(null,null,null,  m_activity.getResources().getDrawable( R.drawable.on));						
							        		}
							        		else
							        		{
							        			ButtonList.get(k).setCompoundDrawablesWithIntrinsicBounds(null,null,null,  m_activity.getResources().getDrawable( R.drawable.off));						
							        		}
							        	}
							        	
							        	adapter.resetArray(dayTime);
							        	adapter.notifyDataSetChanged();
							        	listView.invalidate();
							        }
								});
							}
							
							ArrayList<TimeTableItem> listTime = null;
							
							if (currentDay == Calendar.MONDAY)
							{
								listTime = new ArrayList<TimeTableItem>(mapDayTime.get("Mo"));
								ButtonMo.setCompoundDrawablesWithIntrinsicBounds(null,null,null,  m_activity.getResources().getDrawable( R.drawable.on));
							}
							
							if (currentDay == Calendar.TUESDAY)
							{
								listTime = new ArrayList<TimeTableItem>(mapDayTime.get("Tu"));
								ButtonTu.setCompoundDrawablesWithIntrinsicBounds(null,null,null,  m_activity.getResources().getDrawable( R.drawable.on));
							}
							
							if (currentDay == Calendar.WEDNESDAY)
							{
								listTime = new ArrayList<TimeTableItem>(mapDayTime.get("We"));
								ButtonWe.setCompoundDrawablesWithIntrinsicBounds(null,null,null,  m_activity.getResources().getDrawable( R.drawable.on));
							}
							
							if (currentDay == Calendar.THURSDAY)
							{
								listTime = new ArrayList<TimeTableItem>(mapDayTime.get("Th"));
								ButtonTh.setCompoundDrawablesWithIntrinsicBounds(null,null,null,  m_activity.getResources().getDrawable( R.drawable.on));
							}
							
							if (currentDay == Calendar.FRIDAY)
							{
								listTime = new ArrayList<TimeTableItem>(mapDayTime.get("Fr"));
								ButtonFr.setCompoundDrawablesWithIntrinsicBounds(null,null,null,  m_activity.getResources().getDrawable( R.drawable.on));
							}
							
							if (currentDay == Calendar.SATURDAY)
							{
								listTime = new ArrayList<TimeTableItem>(mapDayTime.get("Sa"));
								ButtonSa.setCompoundDrawablesWithIntrinsicBounds(null,null,null,  m_activity.getResources().getDrawable( R.drawable.on));
							}
							
							if (currentDay == Calendar.SUNDAY)
							{
								listTime = new ArrayList<TimeTableItem>(mapDayTime.get("Su"));
								ButtonSu.setCompoundDrawablesWithIntrinsicBounds(null,null,null,  m_activity.getResources().getDrawable( R.drawable.on));
							}
							
							if (listTime != null)
							{
								adapter = new TimeTableListAdapter(m_activity, R.layout.time_table_item, listTime);
								listView.setAdapter(adapter);
							}
						    
							dialog.show();
				        }
				    });
					
					LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
					layoutItem.addView(routeButton, lp);
				}
			}
		}
		catch(Exception ex)
		{
			Log.v("MainActivity", "trouble ", ex);
		}
	}

}
