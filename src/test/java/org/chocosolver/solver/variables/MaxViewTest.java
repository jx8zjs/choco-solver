/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.variables;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.testng.annotations.Test;

import java.util.Random;

import static org.chocosolver.solver.constraints.checker.DomainBuilder.buildFullDomains;
import static org.chocosolver.solver.search.strategy.Search.randomSearch;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/08/11
 */
public class MaxViewTest {

    public void maxref(Model model, IntVar x, IntVar y, IntVar z) {
        BoolVar[] bs = model.boolVarArray("b", 3);
        model.ifThenElse(bs[0], model.arithm(z, "=", x), model.arithm(z, "!=", x));
        model.ifThenElse(bs[1], model.arithm(z, "=", y), model.arithm(z, "!=", y));
        model.ifThenElse(bs[2], model.arithm(x, ">=", y), model.arithm(x, "<", y));
        model.addClauses(LogOp.or(LogOp.and(bs[0], bs[2]),
                LogOp.and(bs[1], bs[2].not())));
    }

    public void max(Model model, IntVar x, IntVar y, IntVar z) {
        model.max(z, x, y).post();
    }

    @Test(groups="10s", timeOut=60000)
    public void testMax1() {
        Random random = new Random();
        for (int seed = 1; seed < 9999; seed++) {
            random.setSeed(seed);
            int[][] domains = buildFullDomains(3, 1, 15);
            Model ref = new Model();
            {
                IntVar[] xs = new IntVar[3];
                xs[0] = ref.intVar("x", domains[0][0], domains[0][1], true);
                xs[1] = ref.intVar("y", domains[1][0], domains[1][1], true);
                xs[2] = ref.intVar("z", domains[2][0], domains[2][1], true);
                maxref(ref, xs[0], xs[1], xs[2]);
//                SearchMonitorFactory.log(ref, true, true);
                ref.getSolver().setSearch(randomSearch(xs, seed));
            }
            Model model = new Model();
            {
                IntVar[] xs = new IntVar[3];
                xs[0] = model.intVar("x", domains[0][0], domains[0][1], true);
                xs[1] = model.intVar("y", domains[1][0], domains[1][1], true);
                xs[2] = model.intVar("z", domains[1][0], domains[2][1], true);
                max(model, xs[0], xs[1], xs[2]);
//                SearchMonitorFactory.log(solver, true, true);
                model.getSolver().setSearch(randomSearch(xs, seed));
            }
            while (ref.getSolver().solve()) ;
            while (model.getSolver().solve()) ;
            assertEquals(model.getSolver().getSolutionCount(), ref.getSolver().getSolutionCount(), "SOLUTIONS (" + seed + ")");
            assertTrue(model.getSolver().getNodeCount() <= ref.getSolver().getNodeCount(), "NODES (" + seed + ")");
        }
    }

    @Test(groups="10s", timeOut=60000)
    public void testMax2() {
        Random random = new Random();
        for (int seed = 169; seed < 9999; seed++) {
            random.setSeed(seed);
            int[][] domains = buildFullDomains(3, 1, 15, random, random.nextDouble(), random.nextBoolean());
            Model ref = new Model();
            {
                IntVar[] xs = new IntVar[3];
                xs[0] = ref.intVar("x", domains[0]);
                xs[1] = ref.intVar("y", domains[1]);
                xs[2] = ref.intVar("z", domains[2]);
                maxref(ref, xs[0], xs[1], xs[2]);
//                SearchMonitorFactory.log(ref, true, true);
                ref.getSolver().setSearch(randomSearch(xs, seed));
            }
            Model model = new Model();
            {
                IntVar[] xs = new IntVar[3];
                xs[0] = model.intVar("x", domains[0]);
                xs[1] = model.intVar("y", domains[1]);
                xs[2] = model.intVar("z", domains[2]);
                max(model, xs[0], xs[1], xs[2]);
//                SearchMonitorFactory.log(solver, true, true);
                model.getSolver().setSearch(randomSearch(xs, seed));
            }
            while (ref.getSolver().solve()) ;
            while (model.getSolver().solve()) ;
            assertEquals(model.getSolver().getSolutionCount(), ref.getSolver().getSolutionCount(), "SOLUTIONS (" + seed + ")");
            // BEWARE: MAX does not ensure AC, unlike reformulation; so nb of nodes can be different...
//            Assert.assertTrue(solver.getNodeCount() <= ref.getResolver().getMeasures().getNodeCount(), "NODES (" + seed + "): "
//                    + solver.getNodeCount() + " vs. " + ref.getResolver().getMeasures().getNodeCount());
        }
    }
}
