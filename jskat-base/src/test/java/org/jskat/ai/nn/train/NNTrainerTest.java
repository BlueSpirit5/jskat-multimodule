/**
 * JSkat - A skat program written in Java
 * by Jan Schäfer, Markus J. Luzius and Daniel Loreck
 *
 * Version 0.13.0-SNAPSHOT
 * Copyright (C) 2013-05-10
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
package org.jskat.ai.nn.train;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jskat.player.JSkatPlayerResolver;
import org.junit.Test;

public class NNTrainerTest {

	@Test
	public void testCreatePlayerPermutations_NoNNPlayer() {
		List<String> playerTypes = new ArrayList<String>();
		playerTypes.add(JSkatPlayerResolver.HUMAN_PLAYER_CLASS);

		Set<List<String>> permutations = NNTrainer
				.createPlayerPermutations(playerTypes);

		assertEquals(0, permutations.size());
	}

	@Test
	public void testCreatePlayerPermutations_OnlyNNPlayer() {
		List<String> playerTypes = new ArrayList<String>();
		playerTypes.add(NNTrainer.NEURAL_NETWORK_PLAYER_CLASS);

		Set<List<String>> permutations = NNTrainer
				.createPlayerPermutations(playerTypes);

		assertEquals(1, permutations.size());
		List<String> permutation = permutations.iterator().next();
		assertEquals(NNTrainer.NEURAL_NETWORK_PLAYER_CLASS, permutation.get(0));
		assertEquals(NNTrainer.NEURAL_NETWORK_PLAYER_CLASS, permutation.get(1));
		assertEquals(NNTrainer.NEURAL_NETWORK_PLAYER_CLASS, permutation.get(2));
	}

	@Test
	public void testCreatePlayerPermutations_ThreePlayerTypes() {
		List<String> playerTypes = new ArrayList<String>();
		for (String aiPlayer : JSkatPlayerResolver
				.getAllAIPlayerImplementations()) {
			playerTypes.add(aiPlayer);
		}

		Set<List<String>> permutations = NNTrainer
				.createPlayerPermutations(playerTypes);

		assertEquals(19, permutations.size());

		for (List<String> permutation : permutations) {
			assertTrue(permutation
					.contains(NNTrainer.NEURAL_NETWORK_PLAYER_CLASS));
		}
	}
}
