/*
 * Copyright (c) 2002-2007, CWI and INRIA
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of California, Berkeley nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package openllet.aterm.test;

import java.util.List;
import openllet.aterm.AFun;
import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.aterm.ATermFactory;
import openllet.aterm.pure.PureFactory;
import org.junit.Before;
import org.junit.Test;

public class TestFibInterpreted
{

	private ATermFactory	_factory;

	private AFun			zero, suc, plus, fib;
	private ATermAppl		tzero;
	private ATerm			fail;

	private ATerm			lhs[];
	private ATerm			rhs[];

	@Before
	public void setUp()
	{
		_factory = new PureFactory();

		zero = _factory.makeAFun("zero", 0, false);
		suc = _factory.makeAFun("suc", 1, false);
		plus = _factory.makeAFun("plus", 2, false);
		fib = _factory.makeAFun("fib", 1, false);
		tzero = _factory.makeAppl(zero);
		fail = _factory.parse("fail");

		initRules();
	}

	@Test
	public void testFive()
	{
		test1(5);
	}

	public final static void main(final String[] args)
	{
		final TestFibInterpreted t = newTestFibInterpreted(new PureFactory());

		t.initRules();
		t.test1(5);
	}

	public static TestFibInterpreted newTestFibInterpreted(final ATermFactory factory)
	{
		final TestFibInterpreted t = new TestFibInterpreted();
		t._factory = factory;

		t.zero = factory.makeAFun("zero", 0, false);
		t.suc = factory.makeAFun("suc", 1, false);
		t.plus = factory.makeAFun("plus", 2, false);
		t.fib = factory.makeAFun("fib", 1, false);
		t.tzero = factory.makeAppl(t.zero);
		t.fail = factory.parse("fail");

		return t;
	}

	public void initRules()
	{
		lhs = new ATerm[10];
		rhs = new ATerm[10];
		int ruleNumber = 0;

		// fib(zero) -> suc(zero)
		lhs[ruleNumber] = _factory.parse("fib(zero)");
		rhs[ruleNumber] = _factory.parse("suc(zero)");
		ruleNumber++;

		// fib(suc(zero)) -> suc(zero)
		lhs[ruleNumber] = _factory.parse("fib(suc(zero))");
		rhs[ruleNumber] = _factory.parse("suc(zero)");
		ruleNumber++;

		// fib(suc(suc(X))) -> plus(fib(X),fib(suc(X)))
		lhs[ruleNumber] = _factory.parse("fib(suc(suc(<term>)))");
		rhs[ruleNumber] = _factory.parse("plus(fib(<term>),fib(suc(<term>)))");
		ruleNumber++;

		// plus(zero,X) -> X
		lhs[ruleNumber] = _factory.parse("plus(zero,<term>)");
		rhs[ruleNumber] = _factory.parse("<term>");
		ruleNumber++;

		// plus(suc(X),Y) -> plus(X,suc(Y))
		lhs[ruleNumber] = _factory.parse("plus(suc(<term>),<term>)");
		rhs[ruleNumber] = _factory.parse("plus(<term>,suc(<term>))");
		ruleNumber++;

		// congruence (suc)
		lhs[ruleNumber] = _factory.parse("suc(<term>)");
		rhs[ruleNumber] = _factory.parse("suc(<term>)");
		ruleNumber++;

		// congruence (plus)
		lhs[ruleNumber] = _factory.parse("plus(<term>,<term>)");
		rhs[ruleNumber] = _factory.parse("plus(<term>,<term>)");
		ruleNumber++;

	}

	public ATerm oneStep(final ATerm subject)
	{
		int ruleNumber = 0;
		List<Object> list;

		// fib(zero) -> suc(zero)
		list = subject.match(lhs[ruleNumber]);
		if (list != null) return rhs[ruleNumber];
		ruleNumber++;

		// fib(suc(zero)) -> suc(zero)
		list = subject.match(lhs[ruleNumber]);
		if (list != null) return rhs[ruleNumber];
		ruleNumber++;

		// fib(suc(suc(X))) -> plus(fib(X),fib(suc(X)))
		list = subject.match(lhs[ruleNumber]);
		if (list != null)
		{
			final ATerm X = (ATerm) list.get(0);
			list.add(X);
			return _factory.make(rhs[ruleNumber], list);
		}
		ruleNumber++;

		// plus(zero,X) -> X
		list = subject.match(lhs[ruleNumber]);
		if (list != null) return _factory.make(rhs[ruleNumber], list);
		ruleNumber++;

		// plus(suc(X),Y) -> plus(X,suc(Y))
		list = subject.match(lhs[ruleNumber]);
		if (list != null) return _factory.make(rhs[ruleNumber], list);
		ruleNumber++;

		// congruence (suc)
		list = subject.match(lhs[ruleNumber]);
		if (list != null)
		{
			//System.out.println("congsuc"); // applied 1184122 times fir fib(14)
			final ATerm X = (ATerm) list.get(0);
			final ATerm Xp = oneStep(X);
			if (Xp.equals(fail)) return fail;
			list.clear();
			list.add(Xp);
			return _factory.make(rhs[ruleNumber], list);
		}
		ruleNumber++;

		// congruence (plus)
		list = subject.match(lhs[ruleNumber]);
		if (list != null)
		{
			//System.out.println("congplus"); // applied 9159 times fir fib(14)
			final ATerm X = (ATerm) list.get(0);
			final ATerm Xp = oneStep(X);
			if (Xp.equals(fail))
			{
				final ATerm Y = (ATerm) list.get(1);
				final ATerm Yp = oneStep(Y);
				if (Yp.equals(fail)) return fail;
				list.clear();
				list.add(X);
				list.add(Yp);
				return _factory.make(rhs[ruleNumber], list);
			}
			final ATerm Y = (ATerm) list.get(1);
			list.clear();
			list.add(Xp);
			list.add(Y);
			return _factory.make(rhs[ruleNumber], list);
		}
		ruleNumber++;

		return fail;
	}

	public ATerm oneStepInnermost(final ATerm subject)
	{
		List<Object> list;

		// fib(zero) -> suc(zero)
		list = subject.match(lhs[0]);
		if (list != null) return rhs[0];

		// fib(suc(zero)) -> suc(zero)
		list = subject.match(lhs[1]);
		if (list != null) return rhs[1];

		// fib(suc(suc(X))) -> plus(fib(X),fib(suc(X)))
		list = subject.match(lhs[2]);
		if (list != null)
		{
			final ATerm X = (ATerm) list.get(0);
			final ATerm X1 = normalize(_factory.makeAppl(fib, X));
			final ATerm X2 = normalize(_factory.makeAppl(fib, _factory.makeAppl(suc, X)));
			return _factory.makeAppl(plus, X1, X2);
		}

		// plus(zero,X) -> X
		list = subject.match(lhs[3]);
		if (list != null) return (ATerm) list.get(0);

		// plus(suc(X),Y) -> plus(X,suc(Y)))
		list = subject.match(lhs[4]);
		if (list != null) return _factory.make(rhs[4], list);

		return fail;
	}

	public ATerm normalize(final ATerm t_)
	{
		ATerm s = t_;
		ATerm t = t_;
		do
		{
			t = s;
			s = oneStep(t);
		} while (!s.equals(fail));
		return t;
	}

	public void test1(final int n)
	{
		ATermAppl N = tzero;
		for (int i = 0; i < n; i++)
			N = _factory.makeAppl(suc, N);
		final ATerm tfib = _factory.makeAppl(fib, N);
		normalize(tfib);
	}
}
