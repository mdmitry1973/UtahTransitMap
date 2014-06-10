package com.mdmitry1973.utahtransitmap;

import java.io.File;
import java.io.IOException;
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
import android.graphics.drawable.Drawable;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class RoutesDialog extends Dialog {
	
	private	MainActivity m_activity = null;
	private	String m_stop_id = null;
	private Map<String, ArrayList<String>> mapServiceData = null;
	private Map<Button, String> mapStopButtom = new HashMap<Button, String>();
	private File stopFile = null;
	
	private Map<Button, String> mapButtonDayTime = new HashMap<Button, String>();
	private Map<String, ArrayList<String>> mapDayTime = new HashMap<String, ArrayList<String>>();
	private ArrayList<Button> ButtonList = new ArrayList<Button>();
	private ListView listView = null;
	
	
	 private class StableArrayAdapter extends ArrayAdapter<String> {

		    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

		    public StableArrayAdapter(Context context, int textViewResourceId,
		        List<String> objects) {
		      super(context, textViewResourceId, objects);
		      for (int i = 0; i < objects.size(); ++i) {
		        mIdMap.put(objects.get(i), i);
		      }
		    }
		    
		    void resetArray(List<String> objects)
		    {
		    	clear();
		    	mIdMap.clear();
		    	
		    	for (int i = 0; i < objects.size(); ++i) 
		    	{
					mIdMap.put(objects.get(i), i);
					add(objects.get(i));
				}
		    }

		    @Override
		    public long getItemId(int position) {
		      String item = getItem(position);
		      return mIdMap.get(item);
		    }

		    @Override
		    public boolean hasStableIds() {
		      return true;
		    }

		  }
	 
	StableArrayAdapter adapter = null;

	
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
				        	
				        	String route_id = mapStopButtom.get(v);
				        	
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
												
												ArrayList<String> tagMoData = new ArrayList<String>();
												ArrayList<String> tagTuData = new ArrayList<String>();
												ArrayList<String> tagWeData = new ArrayList<String>();
												ArrayList<String> tagThData = new ArrayList<String>();
												ArrayList<String> tagFrData = new ArrayList<String>();
												ArrayList<String> tagSaData = new ArrayList<String>();
												ArrayList<String> tagSuData = new ArrayList<String>();
												
												for(int m = 0; m < trips.getLength(); m++)
												{
													Element tripEl = (Element)trips.item(m);
													String arrival_time = tripEl.getAttribute("arrival_time");
													String service_id = tripEl.getAttribute("service_id");
													ArrayList<String> arrayList = mapServiceData.get(service_id);
													
													if (arrayList.get(0).compareTo("1") == 0)
													{
														tagMoData.add(arrival_time);
													}
													
													if (arrayList.get(1).compareTo("1") == 0)
													{
														tagTuData.add(arrival_time);
													}
													
													if (arrayList.get(2).compareTo("1") == 0)
													{
														tagWeData.add(arrival_time);
													}
													
													if (arrayList.get(3).compareTo("1") == 0)
													{
														tagThData.add(arrival_time);
													}
													
													if (arrayList.get(4).compareTo("1") == 0)
													{
														tagFrData.add(arrival_time);
													}
													
													if (arrayList.get(5).compareTo("1") == 0)
													{
														tagSaData.add(arrival_time);
													}
													
													if (arrayList.get(6).compareTo("1") == 0)
													{
														tagSuData.add(arrival_time);
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
												
												Collection<ArrayList<String>> col = mapDayTime.values();
												Iterator<ArrayList<String>> it = col.iterator();
												ArrayList<String> tagXXData = null;
												
												do
												{
													tagXXData = it.next();
													
													if (tagXXData != null)
													{
														Collections.sort(tagXXData, new Comparator<String> () 
																{
																	@Override
																	public int compare(String lhs,
																			String rhs) 
																	{
																		String[] lhsArr = lhs.split(":");
																		String[] rhsArr = rhs.split(":");
																		
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
							        	ArrayList<String> dayTime = mapDayTime.get(dayName);
							        	
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
							
							/*
							ButtonMo.setOnClickListener(new View.OnClickListener() {
						        @Override
						        public void onClick(View v) {
						           
						        	Log.v("MainActivity", "Clicked ButtonMo");
						        	
						        	String dayName = mapButtonDayTime.get(v);
						        	ArrayList<String> dayTime = mapDayTime.get(dayName);
						        	
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
							
							ButtonSu.setOnClickListener(new View.OnClickListener() {
						        @Override
						        public void onClick(View v) {
						           
						        	Log.v("MainActivity", "Clicked ButtonSu");
						        	
						        	String dayName = mapButtonDayTime.get(v);
						        	ArrayList<String> dayTime = mapDayTime.get(dayName);
						        	
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
							*/
							
							ArrayList<String> listTime = null;
							
							if (currentDay == Calendar.MONDAY)
							{
								listTime = new ArrayList<String>(mapDayTime.get("Mo"));
								ButtonMo.setCompoundDrawablesWithIntrinsicBounds(null,null,null,  m_activity.getResources().getDrawable( R.drawable.on));
							}
							
							if (currentDay == Calendar.TUESDAY)
							{
								listTime = new ArrayList<String>(mapDayTime.get("Tu"));
								ButtonTu.setCompoundDrawablesWithIntrinsicBounds(null,null,null,  m_activity.getResources().getDrawable( R.drawable.on));
							}
							
							if (currentDay == Calendar.WEDNESDAY)
							{
								listTime = new ArrayList<String>(mapDayTime.get("We"));
								ButtonWe.setCompoundDrawablesWithIntrinsicBounds(null,null,null,  m_activity.getResources().getDrawable( R.drawable.on));
							}
							
							if (currentDay == Calendar.THURSDAY)
							{
								listTime = new ArrayList<String>(mapDayTime.get("Th"));
								ButtonTh.setCompoundDrawablesWithIntrinsicBounds(null,null,null,  m_activity.getResources().getDrawable( R.drawable.on));
							}
							
							if (currentDay == Calendar.FRIDAY)
							{
								listTime = new ArrayList<String>(mapDayTime.get("Fr"));
								ButtonFr.setCompoundDrawablesWithIntrinsicBounds(null,null,null,  m_activity.getResources().getDrawable( R.drawable.on));
							}
							
							if (currentDay == Calendar.SATURDAY)
							{
								listTime = new ArrayList<String>(mapDayTime.get("Sa"));
								ButtonSa.setCompoundDrawablesWithIntrinsicBounds(null,null,null,  m_activity.getResources().getDrawable( R.drawable.on));
							}
							
							if (currentDay == Calendar.SUNDAY)
							{
								listTime = new ArrayList<String>(mapDayTime.get("Su"));
								ButtonSu.setCompoundDrawablesWithIntrinsicBounds(null,null,null,  m_activity.getResources().getDrawable( R.drawable.on));
							}
							
							if (listTime != null)
							{
								adapter = new StableArrayAdapter(m_activity, android.R.layout.simple_list_item_1, listTime);
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
