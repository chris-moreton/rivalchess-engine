package com.netsensia.rivalchess.util;

public class SortRoutines 
{
   private static void swap(int[][] array, int i, int j)
   {
      int[] temp = array[i];
      array[i] = array[j];
      array[j] = temp;
   }
	   
	private static int partition(int[][] array, int lo, int hi, int pivotIndex)
	{
	   int[] pivotValue = array[ pivotIndex ];
	 
	   swap(array, pivotIndex, hi); //send pivot item to the back
	 
	   int index = lo; //keep track of where the front ends
	 
	   for (int i = lo; i < hi; i++) //check from the front to the back
	   {
	      //swap if the current value is less than the pivot
	      if ( array[i][2] > pivotValue[2]  )
	      {
	         swap(array, i, index);
	         index++;
	      }
	   }
	 
	   swap(array, hi, index); //put pivot item in the middle
	 
	   return index;
	}

}
