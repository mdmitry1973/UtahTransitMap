package com.mdmitry1973.utahtransitmap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
//import java.util.logging.Level;
//import java.util.logging.Logger;





import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class PrepareData {

	final static String stopFolderData = "stops_data";
	final static String routesFolderData = "routes_data";
	
	public class StopTimeData {
		
		public int trip_id = 0;
		public String arrival_time = "";
		public String departure_time = "";
		public int stop_sequence = 0;
		public int stop_headsign = 0;
		
		public StopTimeData()
		{
			trip_id = 0;
			arrival_time = "";
			departure_time = "";
			stop_sequence = 0;
			stop_headsign = 0;
		}
	}
	
	public PrepareData()
	{
		
	}
	
	public void DeleteRecursive(File fileOrDirectory) {
	    if (fileOrDirectory.isDirectory())
	        for (File child : fileOrDirectory.listFiles())
	            DeleteRecursive(child);

	    fileOrDirectory.delete();
	}
	
	public void mainStartPrepareData(File dataDir, UpdateTimeTablleTask task)
	{
		File stopFile = new File(dataDir + "/stops.txt");
		File stopTimesFile = new File(dataDir + "/stop_times.txt");
		File routesFile = new File(dataDir + "/routes.txt");
		File tripsFile = new File(dataDir + "/trips.txt");
		int  progress = 0;
		
		File TempDir = new File(dataDir + "/TempStopData");
		
		if (TempDir.exists())
		{
			DeleteRecursive(TempDir);
		}
		
		TempDir.mkdir();
		
		if (stopFile.exists())
		{
			try{
				Map<String, ArrayList<String>> mapRouteData = new HashMap<String, ArrayList<String>>();
				Map<String, ArrayList<String>> mapTripData = new HashMap<String, ArrayList<String>>();
				Map<String, ArrayList<StopTimeData>> mapStopTimeData = new HashMap<String, ArrayList<StopTimeData>>();
				
				BufferedReader routesFileBuffer = new BufferedReader(new FileReader(routesFile));
				
				routesFileBuffer.readLine();
				
				while(routesFileBuffer.ready())
				{
					//route_id,agency_id,route_short_name,route_long_name,route_desc,route_type,route_url,route_color,route_text_color
					String routeLine = routesFileBuffer.readLine();
					
					if (routeLine != null)
					{
						String[] routeData = routeLine.split(",");
						
						if (routeData.length > 7)
						{
							String route_id = routeData[0];
							String agency_id = routeData[1];
							String route_short_name = routeData[2];
							String route_long_name = routeData[3];
							String route_desc = routeData[4];
							String route_type = routeData[5];
							String route_url = routeData[6];
							String route_color = routeData[7];
							String route_text_color = routeData[8];
							
							ArrayList<String> arrayList = new ArrayList<String>();
							
							arrayList.add(agency_id);
							arrayList.add(route_short_name);
							arrayList.add(route_long_name);
							arrayList.add(route_desc);
							arrayList.add(route_type);
							arrayList.add(route_url);
							arrayList.add(route_color);
							arrayList.add(route_text_color);
							
							mapRouteData.put(route_id, arrayList);
						}
					}
					else
					{
						Log.v("PrepareData", "Error ");
					}
				}
				
				routesFileBuffer.close();
				
				BufferedReader tripFileBuffer = new BufferedReader(new FileReader(tripsFile));
				
				tripFileBuffer.readLine();
				
				while(tripFileBuffer.ready())
				{
					//route_id,service_id,trip_id,trip_headsign,direction_id,block_id,shape_id
					String tripLine = tripFileBuffer.readLine();
					
					if (tripLine != null)
					{
						String[] tripData = tripLine.split(",");
						
						if (tripData.length > 6)
						{
							String route_id = tripData[0];
							String service_id = tripData[1];
							String trip_id = tripData[2];
							String trip_headsign = tripData[3];
							String direction_id = tripData[4];
							String block_id = tripData[5];
							String shape_id = tripData[6];
							
							ArrayList<String> arrayList = new ArrayList<String>();
							
							arrayList.add(route_id);
							arrayList.add(service_id);
							arrayList.add(trip_headsign);
							arrayList.add(direction_id);
							arrayList.add(block_id);
							arrayList.add(shape_id);
							
							mapTripData.put(trip_id, arrayList);
						}
					}
					else
					{
						Log.v("PrepareData", "Error ");
					}
				}
				
				tripFileBuffer.close();
				
				int nLine = 0;
				
				BufferedReader stopTimeFileBuffer = new BufferedReader(new FileReader(stopTimesFile));
				
				stopTimeFileBuffer.readLine();
				
				while(stopTimeFileBuffer.ready())
				{
					//trip_id,arrival_time,departure_time,stop_id,stop_sequence,stop_headsign,pickup_type,drop_off_type,shape_dist_traveled
					String stopTimeLine = null;
							
					try{		
						stopTimeLine = stopTimeFileBuffer.readLine();
					}
					catch(Exception ex)
					{
					 Log.v("PrepareData", "mapStopTimeData readLine " + ex);
					}
					
					nLine++;
					
					if ((double)nLine%3000.0 == 0.0)
					{
						task.publishProgressData(nLine);
					}
					//Log.v("PrepareData", "readLine=" + nLine);
					
					if (stopTimeLine != null)
					{
						String[] stopTimeData = stopTimeLine.split(",");
						
						
						if (stopTimeData.length > 7)
						{
							File TempStopData = new File(TempDir, stopTimeData[3]);
							
							BufferedWriter stopTimeFileWriter = new BufferedWriter(new FileWriter(TempStopData, true));
							
							stopTimeFileWriter.write(stopTimeLine);
							stopTimeFileWriter.newLine();
							
							stopTimeFileWriter.close();
							
							/*
							StopTimeData stopTimeDataItem = new StopTimeData();
							
							stopTimeDataItem.trip_id = Integer.parseInt(stopTimeData[0]);
							stopTimeDataItem.arrival_time = stopTimeData[1];
							stopTimeDataItem.departure_time = stopTimeData[2];
							stopTimeDataItem.stop_sequence = stopTimeData[4].length() == 0 ? 0 : Integer.parseInt(stopTimeData[4]);
							stopTimeDataItem.stop_headsign = stopTimeData[5].length() == 0 ? 0 : Integer.parseInt(stopTimeData[5]);
							
							if (mapStopTimeData.containsKey(stopTimeData[3]) == true)
							{
								try{
									mapStopTimeData.get(stopTimeData[3]).add(stopTimeDataItem);
								}
								catch(Exception ex)
								{
									  Log.v("PrepareData", "mapStopTimeData get " + ex);
								}
							}
							else
							{
								try{
									ArrayList<StopTimeData> arr = new ArrayList<StopTimeData>();
								
									arr.add(stopTimeDataItem);
								
									mapStopTimeData.put(stopTimeData[3], arr);
								}
								catch(Exception ex)
								{
									  Log.v("PrepareData", "mapStopTimeData put " + ex);
								}
							}
							*/
						}
					}
					else
					{
						Log.v("PrepareData", "Error ");
					}
				}
				
				stopTimeFileBuffer.close();
				
				//activity.handler.sendEmptyMessage(activity.k_event_prepare_data_2);
				nLine = 0;
				task.changeMessageDialog(MainActivity.k_event_prepare_data_2);//666);
				
				BufferedReader stopFileBuffer = new BufferedReader(new FileReader(stopFile));
				
				stopFileBuffer.readLine();
				
				while(stopFileBuffer.ready())
				{
					//stop_id,stop_code,stop_name,stop_desc,stop_lat,stop_lon,zone_id,stop_url,location_type,parent_station
					String stopLine = stopFileBuffer.readLine();
					
					if (stopLine != null)
					{
						String[] stopData = stopLine.split(",");
						
						if (stopData.length > 5)
						{
							String stop_id = stopData[0];
							String stop_code = stopData[1];
							String stop_name = stopData[2];
							String stop_desc = stopData[3];
							String stop_lat = stopData[4];
							String stop_lon = stopData[5];
							
							File stopDataDir = new File(dataDir + "/" + stopFolderData);
							
							if (!stopDataDir.exists())
							{
								stopDataDir.mkdir();
							}
							
							File stopDataFile = new File(dataDir + "/" + stopFolderData + "/" + stop_id);
							
							DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
							DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
							Document document = xmlBuilder.newDocument();
							Element elRoot = document.createElement("Stop");
							
							elRoot.setAttribute("stop_id", stop_id);
							elRoot.setAttribute("stop_code", stop_code);
							elRoot.setAttribute("stop_name", stop_name);
							elRoot.setAttribute("stop_desc", stop_desc);
							elRoot.setAttribute("stop_lat", stop_lat);
							elRoot.setAttribute("stop_id", stop_id);
							elRoot.setAttribute("stop_lon", stop_lon);
							
							document.appendChild(elRoot);
							
							{
								//ArrayList<StopTimeData> arr = mapStopTimeData.get(stop_id);
								ArrayList<String> arr = new ArrayList<String>();//mapStopTimeData.get(stop_id);
								
								File TempStopData = new File(TempDir, stop_id);
								
								if (TempStopData.exists())
								{
									BufferedReader stopTimeReader = new BufferedReader(new FileReader(TempStopData));
									
									while(stopTimeReader.ready())
									{
										String stopTimeLineData = stopTimeReader.readLine();
										
										if (stopTimeLineData != null)
										{
											arr.add(stopTimeLineData);
										}
									}
									
									stopTimeReader.close();
									
									for(int kk = 0; kk < arr.size(); kk++)
									{
										//trip_id,arrival_time,departure_time,stop_id,stop_sequence,stop_headsign,pickup_type,drop_off_type,shape_dist_traveled
										String[] arrStopTimeData = arr.get(kk).split(",");
										
										//StopTimeData item = arr.get(kk);
										
										ArrayList<String> arrayTripDataList = mapTripData.get(arrStopTimeData[0]);//"" + item.trip_id);
										String roudeId = arrayTripDataList.get(0);
										
										Element elRoutesItems = null;
										Element elRoute= null;
										Element elTrip= null;
										
										NodeList nodes = elRoot.getElementsByTagName("routesItems");
										
										if (nodes.getLength() == 0)
										{
											elRoutesItems = document.createElement("routesItems");
											elRoot.appendChild(elRoutesItems);
										}
										else
										{
											elRoutesItems = (Element) nodes.item(0);
										}
										
										Element nodeFirst = (Element)elRoutesItems.getFirstChild();
										
										while(nodeFirst != null)
										{
											String temp = nodeFirst.getAttribute("roudeId") ;
											
											if (temp.compareToIgnoreCase(roudeId) == 0)
											{
												elRoute = nodeFirst;
												break;
											}
											
											nodeFirst = (Element)nodeFirst.getNextSibling();
										}
										
										if (elRoute == null)
										{
											elRoute = document.createElement("Route");
											elRoutesItems.appendChild(elRoute);
											elRoute.setAttribute("roudeId", roudeId);
										}
										
										ArrayList<String> arrayRoudeDataList = mapRouteData.get(roudeId);
										
										elRoute.setAttribute("agency_id", arrayRoudeDataList.get(0));
										elRoute.setAttribute("route_short_name", arrayRoudeDataList.get(1));
										elRoute.setAttribute("route_long_name", arrayRoudeDataList.get(2));
										elRoute.setAttribute("route_desc", arrayRoudeDataList.get(3));
										elRoute.setAttribute("route_type", arrayRoudeDataList.get(4));
										elRoute.setAttribute("route_url", arrayRoudeDataList.get(5));
										elRoute.setAttribute("route_color", arrayRoudeDataList.get(6));
										elRoute.setAttribute("route_text_color", arrayRoudeDataList.get(7));
										
										nodeFirst = (Element)elRoute.getFirstChild();
										
										while(nodeFirst != null)
										{
											String temp = nodeFirst.getAttribute("trip_id") ;
											
											if (temp.compareToIgnoreCase(arrStopTimeData[0]) == 0)
											{
												elTrip = nodeFirst;
												break;
											}
											
											nodeFirst = (Element)nodeFirst.getNextSibling();
										}
										
										if (elTrip == null)
										{
											elTrip = document.createElement("Trip");
											elRoute.appendChild(elTrip);
											elTrip.setAttribute("trip_id", arrStopTimeData[0]);
										}
										
										elTrip.setAttribute("arrival_time", arrStopTimeData[1]);//item.arrival_time);
										elTrip.setAttribute("departure_time", arrStopTimeData[2]);//item.departure_time);
										elTrip.setAttribute("stop_sequence", arrStopTimeData[4]);//"" + item.stop_sequence);
										elTrip.setAttribute("stop_headsign", arrStopTimeData[5]);//"" + item.stop_headsign);
										elTrip.setAttribute("stop_id", stop_id);
										
										elTrip.setAttribute("service_id", arrayTripDataList.get(1));
										elTrip.setAttribute("trip_headsign", arrayTripDataList.get(2));
										elTrip.setAttribute("direction_id", arrayTripDataList.get(3));
										elTrip.setAttribute("block_id", arrayTripDataList.get(4));
										elTrip.setAttribute("shape_id", arrayTripDataList.get(5));
									}
								}
								else
								{
									Log.v("PrepareData", "File not exist " + TempStopData);
								}
							}
							
							// Use a Transformer for output
							TransformerFactory tFactory = TransformerFactory.newInstance();
							Transformer transformer = tFactory.newTransformer();
							
							DOMSource source = new DOMSource(document);
							StreamResult result = new StreamResult(stopDataFile);
							transformer.transform(source, result); 
							
							nLine++;
							
							if ((double)nLine%100.0 == 0.0)
							{
								task.publishProgressData(nLine);
							}
						}
					}
					else
					{
						Log.v("PrepareData", "Error ");
					}
				}
				
				stopFileBuffer.close();
				
				Log.v("PrepareData", "nLine=" + nLine);
				
				
				
				/*
				BufferedReader stopTimeFileBuffer = new BufferedReader(new FileReader(stopTimesFile));
				
				stopTimeFileBuffer.readLine();
				
				//nLine = 0;
				
				while(stopTimeFileBuffer.ready())
				{
					//trip_id,arrival_time,departure_time,stop_id,stop_sequence,stop_headsign,pickup_type,drop_off_type,shape_dist_traveled
					String stopTimeLine = stopTimeFileBuffer.readLine();
					
					nLine++;
					task.publishProgressData(nLine);
					
					if (stopTimeLine != null)
					{
						String[] stopTimeData = stopTimeLine.split(",");
						
						if (stopTimeData.length > 7)
						{
							String trip_id = stopTimeData[0];
							String arrival_time = stopTimeData[1];
							String departure_time = stopTimeData[2];
							String stop_id = stopTimeData[3];
							String stop_sequence = stopTimeData[4];
							String stop_headsign = stopTimeData[5];
							
							File stopDataFile = new File(dataDir + "/" + stopFolderData + "/" + stop_id);
							
							if (stopDataFile.exists())
							{
								ArrayList<String> arrayTripDataList = mapTripData.get(trip_id);
								String roudeId = arrayTripDataList.get(0);
								
								DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
								DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
								Document document = xmlBuilder.parse(stopDataFile);
								Element elRoot = document.getDocumentElement();
								Element elRoutesItems = null;
								Element elRoute= null;
								Element elTrip= null;
								
								NodeList nodes = elRoot.getElementsByTagName("routesItems");
								
								if (nodes.getLength() == 0)
								{
									elRoutesItems = document.createElement("routesItems");
									elRoot.appendChild(elRoutesItems);
								}
								else
								{
									elRoutesItems = (Element) nodes.item(0);
								}
								
								Element nodeFirst = (Element)elRoutesItems.getFirstChild();
								
								while(nodeFirst != null)
								{
									String temp = nodeFirst.getAttribute("roudeId") ;
									
									if (temp.compareToIgnoreCase(roudeId) == 0)
									{
										elRoute = nodeFirst;
										break;
									}
									
									nodeFirst = (Element)nodeFirst.getNextSibling();
								}
								
								if (elRoute == null)
								{
									elRoute = document.createElement("Route");
									elRoutesItems.appendChild(elRoute);
									elRoute.setAttribute("roudeId", roudeId);
								}
								
								ArrayList<String> arrayRoudeDataList = mapRouteData.get(roudeId);
								
								elRoute.setAttribute("agency_id", arrayRoudeDataList.get(0));
								elRoute.setAttribute("route_short_name", arrayRoudeDataList.get(1));
								elRoute.setAttribute("route_long_name", arrayRoudeDataList.get(2));
								elRoute.setAttribute("route_desc", arrayRoudeDataList.get(3));
								elRoute.setAttribute("route_type", arrayRoudeDataList.get(4));
								elRoute.setAttribute("route_url", arrayRoudeDataList.get(5));
								elRoute.setAttribute("route_color", arrayRoudeDataList.get(6));
								elRoute.setAttribute("route_text_color", arrayRoudeDataList.get(7));
								
								nodeFirst = (Element)elRoute.getFirstChild();
								
								while(nodeFirst != null)
								{
									String temp = nodeFirst.getAttribute("trip_id") ;
									
									if (temp.compareToIgnoreCase(trip_id) == 0)
									{
										elTrip = nodeFirst;
										break;
									}
									
									nodeFirst = (Element)nodeFirst.getNextSibling();
								}
								
								if (elTrip == null)
								{
									elTrip = document.createElement("Trip");
									elRoute.appendChild(elTrip);
									elTrip.setAttribute("trip_id", trip_id);
								}
								
								elTrip.setAttribute("arrival_time", arrival_time);
								elTrip.setAttribute("departure_time", departure_time);
								elTrip.setAttribute("stop_sequence", stop_sequence);
								elTrip.setAttribute("stop_headsign", stop_headsign);
								elTrip.setAttribute("stop_id", stop_id);
								
								elTrip.setAttribute("service_id", arrayTripDataList.get(1));
								elTrip.setAttribute("trip_headsign", arrayTripDataList.get(2));
								elTrip.setAttribute("direction_id", arrayTripDataList.get(3));
								elTrip.setAttribute("block_id", arrayTripDataList.get(4));
								elTrip.setAttribute("shape_id", arrayTripDataList.get(5));
								
								// Use a Transformer for output
								TransformerFactory tFactory = TransformerFactory.newInstance();
								Transformer transformer = tFactory.newTransformer();
								
								DOMSource source = new DOMSource(document);
								StreamResult result = new StreamResult(stopDataFile);
								transformer.transform(source, result); 
							}
							else
							{
								Log.v("PrepareData", "not exist " + stopDataFile.getPath());
							}
						}
					}
					else
					{
						Log.v("PrepareData", "Error ");
					}
				}
				
				stopTimeFileBuffer.close();
				*/
			}
			catch(Exception ex)
			{
				  Log.v("PrepareData", "Error " + ex);
			}
		}	
		
		DeleteRecursive(TempDir);
	}
}
