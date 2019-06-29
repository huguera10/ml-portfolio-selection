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
        for (int i = 0; i < portfolio.getN(); i++) {
            model.addLe(w[i], 0.20);
        }

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
            if (infeasibleIndex > 10) {
                System.out.println("Infeasible index too high: " + infeasibleIndex + ". The algorithm will finish.");
                return true;
            }

            System.out.println("*******************\n"
                    + "Generating SSD constraints for infeasible index: " + infeasibleIndex
                    + "\n*******************");

            List<int[]> combinations = generateCombinations(
                    portfolio.getS(),
                    infeasibleIndex
            );

            double t = 0.0;
            for (int i = 0; i < infeasibleIndex; i++) {
                t += (trackedAsset.getReturn(i) / portfolio.getS());
            }

            // create an interatior of sublists to add scenarios constraints in small portions
            // the infeasible index must be checked every time, in order to break the loop between sub combinations
            for (int i = 0; i < combinations.size(); i += 100) {

//            List<int[]> subCombinations = combinations.subList(i, i+1000);
                int maxCombinationsIndex = Math.min(i + 100, combinations.size() - 1);
                setScenariosConstraints(
                        combinations.subList(i, i + maxCombinationsIndex),
                        infeasibleIndex,
                        t
                );

                if (super.solve()) {
                    // if solution is not optimal, just return and don't keep trying other solutions
                    if (!model.getStatus().toString().equals("Optimal")) {
                        return true;
                    }

                    enhancedAsset = buildEnhancedIndex(enhancedAsset);
                    int newInfeasibleIndex = checkOptimality();

                    if (newInfeasibleIndex > infeasibleIndex) {
                        infeasibleIndex = newInfeasibleIndex;
                        break;
                    }
                } else {
                    return false;
                }
            }

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

            if (combinations.size() % 1000000 == 0) {
                return combinations;
            }
        }

        System.out.println("Combinations generated: " + combinations.size());

        return combinations;
    }

    private void sortTrackedIndexReturns() {
        Collections.sort(trackedAsset.getReturns());
    }

    public IloNumVar getV() {
        return v;
    }
}
