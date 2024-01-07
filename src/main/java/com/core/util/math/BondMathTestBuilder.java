package com.core.util.math;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;

/**
 * Created by johnlevidy on 5/18/15.
 *
 * Read a CSV of bloomberg data, turn it into unit tests
 */
public class BondMathTestBuilder {

    public static void main( String args[] ) throws Exception
    {
        StringBuilder builder = new StringBuilder();
        builder.append("double tolerance = 0.0001;\n");
        try( BufferedReader br = new BufferedReader(new FileReader("/Users/jgreco/directmatch/bloomberg.csv"))){
            String line = br.readLine();
            while( line != null )
            {
                               // do things
                String[] csv = line.split(",");
                double coupon = Double.valueOf(csv[2]).doubleValue();
                double price = Double.valueOf(csv[4]).doubleValue();
                double expectedYield = Double.valueOf(csv[5]).doubleValue();
                LocalDate maturityDate = helper(csv[3]);
                LocalDate nextPaymentDate = helper(csv[7]);
                LocalDate previousPaymentDate = helper(csv[6]);
                double yearFracDuration = Double.valueOf( csv[10] ).doubleValue();
                int remainingCoupons = Integer.valueOf(csv[8]).intValue();
                LocalDate settlementDate =helper(csv[11]);

                builder.append("Assert.assertEquals(" + expectedYield + ",");
                builder.append("BondMath.getYield(" + coupon + ", LocalDate.of(" + ( maturityDate.getYear() + 2000 ) + ", " + maturityDate.getMonth().getValue() + ", " + maturityDate.getDayOfMonth() + "), ");
                builder.append("LocalDate.of(" + ( previousPaymentDate.getYear() + 2000 )+ ", " + previousPaymentDate.getMonth().getValue() + ", " + previousPaymentDate.getDayOfMonth() + "), ");
                builder.append("LocalDate.of(" + ( nextPaymentDate.getYear() + 2000 ) + ", " + nextPaymentDate.getMonth().getValue() + ", " + nextPaymentDate.getDayOfMonth() + "), ");
                builder.append( remainingCoupons + ", " + price + ", ");
                builder.append("LocalDate.of(" + (settlementDate.getYear() + 2000) + ", " + settlementDate.getMonthValue() + "," + settlementDate.getDayOfMonth() + ")), tolerance);\n");

                builder.append("Assert.assertEquals(" + yearFracDuration + ",");
                builder.append("BondMath.getDuration(" + coupon + ", ");
                builder.append("LocalDate.of(" + ( 2000 + previousPaymentDate.getYear() ) + ", " + previousPaymentDate.getMonth().getValue() + ", " + previousPaymentDate.getDayOfMonth() + "), ");
                builder.append("LocalDate.of(" + ( 2000 + nextPaymentDate.getYear() ) + ", " + nextPaymentDate.getMonth().getValue() + ", " + nextPaymentDate.getDayOfMonth() + "), ");
                builder.append( remainingCoupons + ", ");
                builder.append( expectedYield + ", " );
                builder.append("LocalDate.of(" + (settlementDate.getYear() + 2000) + ", " + settlementDate.getMonthValue() + "," + settlementDate.getDayOfMonth() + "),");
                builder.append(price + "), tolerance);\n");
                line = br.readLine();
            }
        }
        System.out.println( builder.toString() );
    }

    public static LocalDate helper( String csv )
    {
        String[] split = csv.split("/");
        LocalDate date = LocalDate.of( Integer.valueOf(split[2]).intValue(), Integer.valueOf(split[0]).intValue(), Integer.valueOf(split[1]).intValue());
        return date;
    }
}
