/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.models;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import parameters.Parameters;
import stockmarket.Asset;
import stockmarket.Portfolio;

/**
 *
 * @author hugo
 */
public class SSDOptimatilityChecker {

    private SSDOptimization ssdOptimization;
    private Asset trackedIndex;

    public SSDOptimatilityChecker(Parameters params, Portfolio portfolio, Asset trackedIndex) throws IloException {
        this.trackedIndex = trackedIndex;
        this.ssdOptimization = new SSDOptimization(portfolio, params);

        sortTrackedIndexReturns();
    }

    public void solve() throws IloException {
        ssdOptimization.prepare();

        Asset enhancedIndex = new Asset("enhancedIndex");

        int infeasibleIndex = 1;
        do {
            List<int[]> combinations = generateCombinations(
                    ssdOptimization.getPortfolio().getS(), 
                    infeasibleIndex
            );
            ssdOptimization.setScenariosConstraints(
                    combinations,
                    infeasibleIndex,
                    trackedIndex.getReturn(infeasibleIndex - 1)
            );
            ssdOptimization.solve();

            enhancedIndex = buildEnhancedIndex(enhancedIndex);

            infeasibleIndex = checkOptimality();

        } while (infeasibleIndex < ssdOptimization.getPortfolio().getS());

        ssdOptimization.printAtEnd();
    }
    
    private int checkOptimality() throws IloException {
        IloCplex model = ssdOptimization.getModel();
        double v = model.getValue(ssdOptimization.getV());
                
        for (int s = 0; s < ssdOptimization.getPortfolio().getS(); s++) {
            if(trackedIndex.getReturn(s) > v){
                return s;
            }
        }

        return ssdOptimization.getPortfolio().getS();
    }

    private Asset buildEnhancedIndex(Asset enhancedIndex) {
        for (int s = 0; s < ssdOptimization.getPortfolio().getS(); s++) {
            double enhancedReturn = 0.0;
            for (int i = 0; i < ssdOptimization.getPortfolio().getN(); i++) {

                Asset asset = ssdOptimization.getPortfolio().getAsset(i);
                if (asset.getWeigth() > 0.0) {
                    enhancedReturn += (asset.getReturn(s) * asset.getWeigth());
                }
            }
            if (enhancedIndex.getReturns().size() < ssdOptimization.getPortfolio().getS()) {
                enhancedIndex.addReturn(enhancedReturn);
            } else {
                enhancedIndex.setReturn(s, enhancedReturn);
            }
        }
        return enhancedIndex;
    }
    
    private List<int[]> generateCombinations(int n, int r) { //n = total elements in array; r = size of combinations
        List<int[]> combinations = new ArrayList<>();
        int[] combination = new int[r];

        // initialize with lowest lexicographic combination
        for (int i = 0; i < r; i++) {
            combination[i] = i;
        }

        while (combination[r - 1] < n) {
            combinations.add(combination.clone());

            // generate next combination in lexicographic order
            int t = r - 1;
            while (t != 0 && combination[t] == n - r + t) {
                t--;
            }
            combination[t]++;
            for (int i = t + 1; i < r; i++) {
                combination[i] = combination[i - 1] + 1;
            }
        }

        return combinations;
    }

    private void sortTrackedIndexReturns() {
        Collections.sort(trackedIndex.getReturns());
    }

}
