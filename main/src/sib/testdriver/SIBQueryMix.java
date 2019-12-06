package sib.testdriver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import sib.bibm.AbstractParameterPool;
import sib.bibm.Query;
import sib.bibm.QueryMix;


public class SIBQueryMix extends QueryMix {
    private Random seedGen;

    public SIBQueryMix(AbstractParameterPool parameterPool, File queryDir, Long seed) throws IOException {
        super(parameterPool, queryDir);
        
        if (seed!=null) {
            seedGen = new Random(seed);
        }
   }
    /**
     * TODO BSBM only
     *  if seed was set, performs permutation
     * @param run
     * @return 
     */
    public Query[] getPermutatedQueries() {
        int len = queryNames.length;
        Query[]  res=new Query[len];
        if (seedGen==null) {
            for (int k=0; k<len; k++) {
                res[k]=queries.get(queryNames[k]);
            }
            return res;
        }
        ArrayList<String> copy=new ArrayList<String>(len);
        for (String e: queryNames) {
            copy.add(e);
        }
        for (int k=0; k<len; k++) {
            int nextInt = seedGen.nextInt(copy.size());
            String nextQueryN = copy.remove(nextInt);
            res[k]=queries.get(nextQueryN);
        }
        return res;
    }


}
