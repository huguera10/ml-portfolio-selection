/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization.models.cvar;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import optimization.models.BaseOptimization;
import parameters.Parameters;
import stockmarket.Portfolio;

/**
 *
 * @author hugo
 */
public class CvarOptimization extends BaseOptimization {

    private IloNumVar VaR;
    private IloNumVar[] d;
    private IloNumVar[] r;

    public static final double PORTFOLIO_MEAN = 0.0;

    public CvarOptimization(Portfolio portfolio, Parameters params) throws IloException {
        super(portfolio, params);
        super.prepare();
    }

    @Override
    public void createDecisionVariables() throws IloException {
        createBasicDecisionVariables();

        VaR = model.numVar(-BIG, BIG);

        d = new IloNumVar[portfolio.getS()];
        r = new IloNumVar[portfolio.getS()];
        for (int s = 0; s < portfolio.getS(); s++) {
            d[s] = model.numVar(-BIG, BIG);
            r[s] = model.numVar(-BIG, BIG);
        }
    }

    @Override
    public void setObjectiveFunction() throws IloException {
        IloLinearNumExpr fo = model.linearNumExpr();

        for (int s = 0; s < portfolio.getS(); s++) {
            fo.addTerm(1.0, d[s]);
        }

        model.addMinimize(model.sum(
                VaR,
                model.prod(1.0 / (double) (portfolio.getS() * params.getAlpha()), fo)
        ));
    }

    @Override
    public void setConstraints() throws IloException {
        IloLinearNumExpr expr;

        for (int s = 0; s < portfolio.getS(); s++) {
            model.addGe(d[s], model.sum(model.prod(-1, VaR), model.prod(-1, r[s])));
            model.addGe(d[s], 0.0);
        }

        for (int s = 0; s < portfolio.getS(); s++) {
            expr = model.linearNumExpr();

            for (int i = 0; i < portfolio.getN(); i++) {
                expr.addTerm(portfolio.getAsset(i).getReturn(s), w[i]);
            }
            model.addEq(r[s], expr);
        }

        expr = model.linearNumExpr();
        for (int i = 0; i < portfolio.getN(); i++) {
            expr.addTerm(portfolio.getAssetMeanReturn(i), w[i]);
        }
        model.addGe(expr, PORTFOLIO_MEAN);

        setWeightsConstraints();
    }

    @Override
    public void printAtEnd() throws IloException {
        printPortfoliosWeights();

        System.out.println("-----------------------------------------");
    }

}
