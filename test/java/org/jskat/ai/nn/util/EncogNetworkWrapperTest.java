/**
 * JSkat - A skat program written in Java
 * by Jan Schäfer, Markus J. Luzius and Daniel Loreck
 *
 * Version 0.11.0
 * Copyright (C) 2012-08-28
 *
 * Licensed under the Apache License, Version 2.0. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jskat.ai.nn.util;

import static org.junit.Assert.fail;

import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataPair;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.Propagation;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.jskat.AbstractJSkatTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncogNetworkWrapperTest extends AbstractJSkatTest {

	private static final double MIN_DIFF = 0.001;
	private static Logger log = LoggerFactory
			.getLogger(EncogNetworkWrapperTest.class);

	@Test
	public void testBooleanFunction() {

		int[] hiddenNeurons = { 3 };
		NetworkTopology topo = new NetworkTopology(3, 1, 1, hiddenNeurons);
		INeuralNetwork network = new EncogNetworkWrapper(topo, true);

		double[][] input = { { 1.0, 1.0, 1.0 }, { 1.0, 1.0, 0.0 },
				{ 1.0, 0.0, 1.0 }, { 1.0, 0.0, 0.0 }, { 0.0, 1.0, 1.0 },
				{ 0.0, 1.0, 0.0 }, { 0.0, 0.0, 1.0 }, { 0.0, 0.0, 0.0 } };
		double[][] output = { { 1.0 }, // A and B or C
				{ 1.0 }, { 1.0 }, { 0.0 }, { 1.0 }, { 0.0 }, { 1.0 }, { 0.0 } };

		// assertTrue(network.adjustWeights(input, output) < 0.5);
	}

	@Test
	public void testXOR() {

		int[] hiddenNeurons = { 3 };
		NetworkTopology topo = new NetworkTopology(2, 1, 1, hiddenNeurons);
		INeuralNetwork network = new EncogNetworkWrapper(topo, false);
		network.resetNetwork();

		double[][] input = { { 1.0, 1.0 }, { 1.0, 0.0 }, { 0.0, 1.0 },
				{ 0.0, 0.0 } };
		double[][] output = { { 0.0 }, // A XOR B
				{ 1.0 }, { 1.0 }, { 0.0 } };

		double error = 1000.0;
		double[] inputs = new double[2];
		double[] outputs = new double[1];
		int i = 0;
		int runs = 0;

		while (error > MIN_DIFF && runs < 10000) {
			network.adjustWeights(input[i], output[i]);
			error = network.getAvgDiff();
			i = (i + 1) % input.length;
			runs++;
		}

		if (runs == 10000) {
			fail("Needed more than 10000 runs. Error: " + error);
		} else {
			log.debug("Needed " + runs + " to learn.");
		}
	}

	@Test
	public void testXORDirect() {

		BasicNetwork network = new BasicNetwork();
		network.addLayer(new BasicLayer(new ActivationSigmoid(), true, 2));
		network.addLayer(new BasicLayer(new ActivationSigmoid(), true, 3));
		network.addLayer(new BasicLayer(new ActivationSigmoid(), true, 1));
		network.getStructure().finalizeStructure();
		network.reset();

		BasicMLDataSet trainingSet = new BasicMLDataSet();
		double[][] input = { { 1.0, 1.0 }, { 1.0, 0.0 }, { 0.0, 1.0 },
				{ 0.0, 0.0 } };
		double[][] output = { { 0.0 }, // A XOR B
				{ 1.0 }, { 1.0 }, { 0.0 } };

		for (int i = 0; i < input.length; i++) {
			trainingSet.add(new BasicMLDataPair(new BasicMLData(input[i]),
					new BasicMLData(output[i])));
		}

		Propagation trainer = new ResilientPropagation(network, trainingSet);

		double error = 1000.0;
		int runs = 0;

		while (error > MIN_DIFF && runs < 200) {
			trainer.iteration();
			error = trainer.getError();
			runs++;
		}

		if (runs == 200) {
			fail("Needed more than 200 runs. Error: " + error);
		} else {
			log.debug("Needed " + runs + " to learn.");
		}
	}
}
