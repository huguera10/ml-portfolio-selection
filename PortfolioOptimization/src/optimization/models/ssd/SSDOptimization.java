/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.models.ssd;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import java.util.Collections;
import java.util.LinkedList;
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
    private Asset trackedAsset;

    public SSDOptimization(Portfolio portfolio, Parameters params, Asset trackedAsset) throws IloException {
        super(portfolio, params);

        this.trackedAsset = trackedAsset;

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

    @Override
    public boolean solve() throws IloException {
        prepare();

        Asset enhancedAsset = new Asset("enhancedAsset");

        int infeasibleIndex = 1;
        do {
            List<int[]> combinations = generateCombinations(
                    portfolio.getS(),
                    infeasibleIndex
            );

            double t = 0.0;
            for (int i = 0; i < infeasibleIndex; i++) {
                t += (trackedAsset.getReturn(i) / portfolio.getS());
            }
            setScenariosConstraints(
                    combinations,
                    infeasibleIndex,
                    t
            );
            super.solve();

            enhancedAsset = buildEnhancedIndex(enhancedAsset);

            infeasibleIndex = checkOptimality();

        } while (infeasibleIndex < portfolio.getS());

        printAtEnd();

        return true;
    }

    private int checkOptimality() throws IloException {
        double v = model.getValue(this.v);

        double t = 0.0;
        for (int s = 0; s < portfolio.getS(); s++) {
            t += (trackedAsset.getReturn(s) / portfolio.getS());

            if (t > v) {
                return s;
            }
        }

        return portfolio.getS();
    }

    private Asset buildEnhancedIndex(Asset enhancedAsset) {
        for (int s = 0; s < portfolio.getS(); s++) {
            double enhancedReturn = 0.0;

            for (int i = 0; i < portfolio.getN(); i++) {

                Asset asset = portfolio.getAsset(i);
                if (asset.getWeigth() > 0.0) {
                    enhancedReturn += (asset.getReturn(s) * asset.getWeigth());
                }
            }
            if (enhancedAsset.getReturns().size() < portfolio.getS()) {
                enhancedAsset.addReturn(enhancedReturn);
            } else {
                enhancedAsset.setReturn(s, enhancedReturn);
            }
        }
        return enhancedAsset;
    }

    private List<int[]> generateCombinations(int n, int r) { //n = total elements in array; r = size of combinations
        List<int[]> combinations = new LinkedList<>();
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

//            if (combinations.size() % 10000 == 0) {
//                return combinations;
//            }
        }

        return combinations;
    }

    private void sortTrackedIndexReturns() {
        Collections.sort(trackedAsset.getReturns());
    }

    public IloNumVar getV() {
        return v;
    }
}
