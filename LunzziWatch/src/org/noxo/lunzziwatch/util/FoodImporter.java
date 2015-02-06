package org.noxo.lunzziwatch.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.noxo.lunzziwatch.model.Food;
import org.json.*;

import android.util.Log;

public class FoodImporter {
	
	final static String TAG = FoodImporter.class.getSimpleName();

	public static List<Food> importFood(String stringUrl)
	{
		BufferedReader reader = null;
		
		try {
			
			Log.d(TAG, "fetching food from: " + stringUrl);
			
			List<Food> foodList = new ArrayList<Food>();
			
			URL url = new URL(stringUrl);
			URLConnection conn = url.openConnection();
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
			StringBuffer sb = new StringBuffer();
			String line = null;
	        
			while ((line = reader.readLine()) != null) {
	            sb.append(line);
	        }
	        
			Log.d(TAG, "parsing json");
			
			JSONObject root = new JSONObject(sb.toString());
			JSONArray courses = (JSONArray) root.get("courses");
			
			for (int i=0;i<courses.length();i++)
			{
				
				JSONObject jsonFood = courses.getJSONObject(i);
				
				Food food = new Food();
				
				if (jsonFood.has("category"))
				{
					food.setCategory(jsonFood.getString("category"));
				}
				
				if (jsonFood.has("title_fi"))
				{
					food.setTitle(jsonFood.getString("title_fi"));
				}
				
				if (jsonFood.has("properties"))
				{
					food.setProps(jsonFood.getString("properties"));
				}
				
				if (jsonFood.has("price"))
				{
					food.setPrice(jsonFood.getString("price"));
				}
				
				foodList.add(food);
				
			}
			
			Log.d(TAG, "json parsed");
			
			return foodList;
			
		} catch (Exception e) {
			Log.e(TAG, "can't fetch", e);
			return null;
		}
		finally
		{
			try
			{
				if (reader != null)
					reader.close();
			} catch (Exception e)
			{
				Log.w(TAG, e);
			}
		}

	}
	
}
