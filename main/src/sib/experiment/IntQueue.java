package sib.experiment;

import java.util.ArrayList;

public class IntQueue extends ArrayList {

   /*
      boolean isEmpty ()
   */

   public void addRear (int item) {
      add (new Integer (item));
   }

   public int removeFront () {
      return ((Integer)remove(0)).intValue();
   }
   
}
