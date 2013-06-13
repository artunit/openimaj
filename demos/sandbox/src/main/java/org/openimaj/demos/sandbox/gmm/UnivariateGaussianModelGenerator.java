/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.demos.sandbox.gmm;

import java.util.Arrays;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.GammaDistributionImpl;
import org.apache.commons.math.distribution.NormalDistributionImpl;
import org.apache.commons.math.special.Gamma;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;

/**
 * P( mean , variance ) = P(mean | variance) P(variance)
 * 
 * P(mean | variance ) = Norm (mean | mean_0, variance) P(variance) = Gamma
 * (variance | a_0, b_0)
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class UnivariateGaussianModelGenerator {
	private static final double EPS = 0.000001;
	private double variance;
	private double mean0;
	private GammaDistributionImpl gamma;
	private double gammaAlpha;

	public UnivariateGaussianModelGenerator(double mean0, double a0, double b0) {
		this.gamma = new GammaDistributionImpl(a0, b0);
		this.gammaAlpha = Math.exp(Gamma.logGamma(a0));
		this.variance = a0 * b0;
		this.mean0 = mean0;
	}

	public double sample() throws MathException {
		double var;
		var = this.gamma.sample();
		final NormalDistributionImpl norm = new NormalDistributionImpl(mean0, var);
		final double mean = norm.sample();
		final NormalDistributionImpl liklihood = new NormalDistributionImpl(mean, Math.sqrt(var));
		return liklihood.sample();
	}

	public double joinProbability(double mean, double variance) throws MathException {
		variance += EPS;
		final double jp = jointProbUn(mean, variance);
		final double norm = jointProbUn(this.expectedMean(), this.expectedVar());
		return jp;
	}

	private double jointProbUn(double mean, double variance) {
		final double pvar = this.gamma.density(variance);
		final double pmean_var = new NormalDistributionImpl(mean0, 1 / variance).density(mean);
		return pvar * pmean_var;
	}

	public static void main(String[] args) throws MathException {
		final UnivariateGaussianModelGenerator gen = new UnivariateGaussianModelGenerator(0, 3, 0.2);
		System.out.println("Expected variance: " + gen.expectedVar());
		System.out.println("Expected mean: " + gen.expectedMean());
		final float[] gamm = new float[100];
		for (int i = 0; i < gamm.length; i++) {
			gamm[i] = (float) gen.gamma.density(i);
		}

		System.out.println(Arrays.toString(gamm));
		final MBFImage img = new MBFImage(400, 400, 3);
		final MBFImage contours = new MBFImage(400, 400, 3);
		final Float[] ONE = RGBColour.GREEN;
		final Float[] ZERO = RGBColour.RED;
		final int DIM = 400;
		final int CENTERX = DIM / 2;
		final int CENTERY = DIM;
		final int CXRNG = 6;
		final int CYRNG = 6;
		final float WEIGHTX = DIM / (float) CXRNG;
		final float WEIGHTY = DIM / (float) CYRNG;
		double sumJoint = 0d;
		for (int y = 0; y < DIM; y++) {
			for (int x = 0; x < DIM; x++) {
				final float cx = (CENTERX - x) / WEIGHTX;
				final float cy = (CENTERY - y) / WEIGHTY;

				// System.out.printf("x,y,cx,cy: %1.3f, %1.3f, %1.3f, %1.3f\n",(float)x,(float)y,cx,cy);

				final float val = (float) gen.joinProbability(cx, cy);
				sumJoint += val;
				final float inv = 1 - val;
				final Float[] col = new Float[] {
						ONE[0] * val + ZERO[0] * inv,
						ONE[1] * val + ZERO[1] * inv,
						ONE[2] * val + ZERO[2] * inv
				};

				img.setPixel(x, y, col);
				final double rng = 0.05;
				if (val > rng - 0.003 && val < rng + 0.003) {
					contours.setPixel(x, y, RGBColour.WHITE);
				}
			}
		}
		DisplayUtilities.display(img);
		DisplayUtilities.display(contours);
	}

	private double expectedMean() {
		return this.mean0;
	}

	private double expectedVar() {
		return this.gamma.getAlpha() * this.gamma.getBeta();
	}
}
