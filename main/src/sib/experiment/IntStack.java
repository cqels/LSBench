package sib.experiment;

import java.util.ArrayList;

public class IntStack extends ArrayList {

   /*
      boolean isEmpty ()
   */

   public void addFront (int item) {
      add (0, new Integer (item));
   }

   public int removeFront () {
      return ((Integer)remove(0)).intValue();
   }
   
}
