/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.models.ssd;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import optimization.models.BaseOptimization;
import parameters.Parameters;
import stockmarket.Asset;
import stockmarket.Portfolio;

/**
 *
 * @author hugo
 */
public class SSDOptimization extends BaseOptimization {

    private IloNumVar v;
    private Asset trackedIndex;

    public SSDOptimization(Portfolio portfolio, Parameters params, Asset trackedIndex) throws IloException {
        super(portfolio, params);
        this.trackedIndex = trackedIndex;
        
        sortTrackedIndexReturns();
    }

    @Override
    public void createDecisionVariables() throws IloException {
        createBasicDecisionVariables();

        v = model.numVar(-BIG, BIG);
    }

    @Override
    public void setObjectiveFunction() throws IloException {
        model.addMaximize(v);
    }

    @Override
    public void setConstraints() throws IloException {
        setWeightsConstraints();
    }

    @Override
    public void printAtEnd() throws IloException {
        printPortfoliosWeights();

        System.out.println("-----------------------------------------");
    }

    public void setScenariosConstraints(List<int[]> combinations, int nScenariosConstraint, double t) throws IloException {
        IloLinearNumExpr expr;
        double constant = 1.0 / portfolio.getS();

        for (int[] combination : combinations) {

            expr = model.linearNumExpr();
            for (int s_index = 0; s_index < nScenariosConstraint; s_index++) {
                int s = combination[s_index];

                for (int i = 0; i < portfolio.getN(); i++) {

                    expr.addTerm(portfolio.getAsset(i).getReturn(s), w[i]);
                }
            }
            model.addLe(v, model.diff(model.prod(constant, expr), t));
        }
    }
    
    public boolean solve() throws IloException {
        prepare();

        Asset enhancedIndex = new Asset("enhancedIndex");

        int infeasibleIndex = 1;
        do {
            List<int[]> combinations = generateCombinations(
                    portfolio.getS(), 
                    infeasibleIndex
            );
            setScenariosConstraints(
                    combinations,
                    infeasibleIndex,
                    trackedIndex.getReturn(infeasibleIndex - 1)
            );
            super.solve();

            enhancedIndex = buildEnhancedIndex(enhancedIndex);

            infeasibleIndex = checkOptimality();

        } while (infeasibleIndex < portfolio.getS());

        printAtEnd();
        
        return true;
    }
    
    private int checkOptimality() throws IloException {
        double v = model.getValue(this.v);
                
        for (int s = 0; s < portfolio.getS(); s++) {
            if(trackedIndex.getReturn(s) > v){
                return s;
            }
        }

        return portfolio.getS();
    }

    private Asset buildEnhancedIndex(Asset enhancedIndex) {
        for (int s = 0; s < portfolio.getS(); s++) {
            double enhancedReturn = 0.0;
            for (int i = 0; i < portfolio.getN(); i++) {

                Asset asset = portfolio.getAsset(i);
                if (asset.getWeigth() > 0.0) {
                    enhancedReturn += (asset.getReturn(s) * asset.getWeigth());
                }
            }
            if (enhancedIndex.getReturns().size() < portfolio.getS()) {
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

    public IloNumVar getV() {
        return v;
    }
}
