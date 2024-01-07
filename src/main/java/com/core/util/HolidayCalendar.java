package com.core.util;

import com.core.util.log.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class HolidayCalendar {
	// unique by year, month, day, so we're good
	final Set<LocalDate> holidaysSet = new HashSet<LocalDate>();
	final Set<LocalDate> halfDaysSet = new HashSet<LocalDate>();

	public HolidayCalendar(Log log) {
		// load from file
		try {
			BufferedReader br = new BufferedReader(new FileReader("holidays.txt"));
			String line;
			while ((line = br.readLine()) != null) {
				String[] split = line.split(",");
				LocalDate date = TimeUtils.toLocalDate(Integer.valueOf(split[0]).intValue());
				// be able to handle it 
				if( split.length > 1 && split[1].toLowerCase().equals("half") )
				{
					halfDaysSet.add(date);
				}
				else
				{
					holidaysSet.add(date);
				}
			}
		}
		catch (Exception e) {
			log.error(log.log().add("Could not find file holidays.txt or failure to load holiday calendar... assuming no holidays"));
		}
	}

	public boolean isTradingDate( LocalDate localDate )
	{
		return !holidaysSet.contains(localDate) && !(localDate.getDayOfWeek() == DayOfWeek.SATURDAY || localDate.getDayOfWeek() == DayOfWeek.SUNDAY );  
	}
}
