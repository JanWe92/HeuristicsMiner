package org.processmining.plugins.heuristicsnet.miner.heuristics.converter;

import java.util.HashMap;
import java.util.LinkedList;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.connections.flexiblemodel.FlexEndTaskNodeConnection;
import org.processmining.models.connections.flexiblemodel.FlexStartTaskNodeConnection;
import org.processmining.models.flexiblemodel.EndTaskNodesSet;
import org.processmining.models.flexiblemodel.Flex;
import org.processmining.models.flexiblemodel.FlexFactory;
import org.processmining.models.flexiblemodel.FlexNode;
import org.processmining.models.flexiblemodel.SetFlex;
import org.processmining.models.flexiblemodel.StartTaskNodesSet;
import org.processmining.models.heuristics.HeuristicsNet;
import org.processmining.models.heuristics.impl.HNSet;
import org.processmining.models.heuristics.impl.HNSubSet;
import org.processmining.plugins.heuristicsnet.AnnotatedHeuristicsNet;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.operators.Join;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.operators.Split;


@Plugin(name = "Convert Heuristics net into Flexible model", 
		parameterLabels = {"HeuristicsNet"},
		returnLabels = {"Flexible model", "Start task nodes", "End task nodes"},
		returnTypes = {Flex.class, StartTaskNodesSet.class, EndTaskNodesSet.class},
		userAccessible = true,
		help = "Converts heuristics net (or causal matrices) into Flexible model")
public class HeuristicsNetToFlexConverter {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "J.T.S. Ribeiro", email = "j.t.s.ribeiro@tue.nl", website = "http://is.tm.tue.nl/staff/jribeiro", pack = "HeuristicsMiner")
	@PluginVariant(variantLabel = "Default Settings", requiredParameterLabels = { 0 })
	public static Object[] converter(PluginContext context, HeuristicsNet hn) {
		
		final Flex flexDiagram = FlexFactory.newFlex("FM");
		
		if(hn instanceof AnnotatedHeuristicsNet){
			
			AnnotatedHeuristicsNet ahn = (AnnotatedHeuristicsNet) hn;
			HashMap<String, String> keys = ahn.getInvertedKeys();
			
			FlexNode[] nodes = new FlexNode[keys.size()];
			for(java.util.Map.Entry<String, String> entry : keys.entrySet()){
				
				int index = Integer.valueOf(entry.getKey());
				
				nodes[index] = flexDiagram.addNode(entry.getValue());
			}
			
			for(String task : keys.keySet()){
				
				int index = Integer.valueOf(task);
				
				Join join = ahn.getJoin(task);
				HNSubSet inputs = join.getElements();
				
				Split split = ahn.getSplit(task);
				HNSubSet outputs = split.getElements();
				
				for(int i = 0; i < outputs.size(); i++){
					
					flexDiagram.addArc(nodes[index], nodes[outputs.get(i)]);
				}
				
				if(inputs.size() > 0){
					
					for(String code : join.getLearnedPatterns().keySet()){
						
						SetFlex temp = new SetFlex();
						for(int i = 0; i < code.length(); i++){
							
							if(code.charAt(i) == '1') temp.add(nodes[inputs.get(i)]);
						}
						nodes[index].addInputNodes(temp);
					}
				}
				else nodes[index].addInputNodes(new SetFlex());

				if(outputs.size() > 0){
					
					for(String code : split.getLearnedPatterns().keySet()){
						
						SetFlex temp = new SetFlex();
						for(int i = 0; i < code.length(); i++){
							
							if(code.charAt(i) == '1') temp.add(nodes[outputs.get(i)]);
						}
						nodes[index].addOutputNodes(temp);
					}
				}
				else nodes[index].addOutputNodes(new SetFlex());
				
			}
			
			for(int i = 0; i < nodes.length; i++) nodes[i].commitUpdates();
			
			StartTaskNodesSet startTaskNodes = new StartTaskNodesSet();
			HNSubSet startTasks = ahn.getStartActivities();
			for(int i = 0; i < startTasks.size(); i++){
				
				SetFlex temp = new SetFlex();
				temp.add(nodes[startTasks.get(i)]);
				startTaskNodes.add(temp);
			}			
			context.addConnection(new FlexStartTaskNodeConnection(flexDiagram.getLabel(), flexDiagram, startTaskNodes));
			
			EndTaskNodesSet endTaskNodes = new EndTaskNodesSet();
			HNSubSet endTasks = ahn.getEndActivities();
			for(int i = 0; i < endTasks.size(); i++){
				
				SetFlex temp = new SetFlex();
				temp.add(nodes[endTasks.get(i)]);
				endTaskNodes.add(temp);
			}
			context.addConnection(new FlexEndTaskNodeConnection(flexDiagram.getLabel(), flexDiagram, endTaskNodes));
			
			return new Object[] { flexDiagram, startTaskNodes, endTaskNodes };
		}
		else{
			
			XEventClass[] activities = hn.getActivitiesMappingStructures().getActivitiesMapping();
			
			FlexNode[] nodes = new FlexNode[activities.length];
			for (int activityIndex = 0; activityIndex < activities.length; activityIndex++) {
				
				nodes[activityIndex] = flexDiagram.addNode(activities[activityIndex].toString());
			}
			
			for (int activityIndex = 0; activityIndex < activities.length; activityIndex++) {
				
				HNSet inputActivitiesSet = hn.getInputSet(activityIndex);
				HNSet outputActivitiesSet = hn.getOutputSet(activityIndex);
				
				LinkedList<Conjunction> inputs = computeConjunctions(inputActivitiesSet);
				LinkedList<Conjunction> outputs = computeConjunctions(outputActivitiesSet);
				
				if(inputs.size() > 0){
					
					for(Conjunction ic : inputs){
						
						SetFlex temp = new SetFlex();
						for(int i = 0; i < ic.size(); i++){
							
							temp.add(nodes[ic.getElement(i)]);
						}
						nodes[activityIndex].addInputNodes(temp);
					}
				}
				else nodes[activityIndex].addInputNodes(new SetFlex());
				
				if(outputs.size() > 0){
					
					for(Conjunction ic : outputs){
						
						SetFlex temp = new SetFlex();
						for(int i = 0; i < ic.size(); i++){
							
							FlexNode tempNode = nodes[ic.getElement(i)];
							
							temp.add(tempNode);
							
							if(flexDiagram.getArc(nodes[activityIndex], tempNode) == null)
								flexDiagram.addArc(nodes[activityIndex], tempNode);
						}
						nodes[activityIndex].addOutputNodes(temp);
					}
				}
				else nodes[activityIndex].addOutputNodes(new SetFlex());
			}
			
			for(int i = 0; i < nodes.length; i++) nodes[i].commitUpdates();
			
			if(hn.getStartActivities().size() == 0){
				
				HNSet[] inputSets = hn.getInputSets();
				HNSubSet startActivities = new HNSubSet();
				for(int i = 0; i < inputSets.length; i++){
					
					if(inputSets[i].size() == 0) startActivities.add(i);
				}
				if(startActivities.size() > 0) hn.setStartActivities(startActivities);
			}
			
			StartTaskNodesSet startTaskNodes = new StartTaskNodesSet();
			HNSubSet startTasks = hn.getStartActivities();
			for(int i = 0; i < startTasks.size(); i++){
				
				SetFlex temp = new SetFlex();
				temp.add(nodes[startTasks.get(i)]);
				startTaskNodes.add(temp);
			}			
			context.addConnection(new FlexStartTaskNodeConnection(flexDiagram.getLabel(), flexDiagram, startTaskNodes));
			
			if(hn.getEndActivities().size() == 0){
				
				HNSet[] outputSets = hn.getOutputSets();
				HNSubSet endActivities = new HNSubSet();
				for(int i = 0; i < outputSets.length; i++){
					
					if(outputSets[i].size() == 0) endActivities.add(i);
				}
				if(endActivities.size() > 0) hn.setEndActivities(endActivities);
			}
			
			EndTaskNodesSet endTaskNodes = new EndTaskNodesSet();
			HNSubSet endTasks = hn.getEndActivities();
			for(int i = 0; i < endTasks.size(); i++){
				
				SetFlex temp = new SetFlex();
				temp.add(nodes[endTasks.get(i)]);
				endTaskNodes.add(temp);
			}
			context.addConnection(new FlexEndTaskNodeConnection(flexDiagram.getLabel(), flexDiagram, endTaskNodes));
			
			return new Object[] { flexDiagram, startTaskNodes, endTaskNodes };
		}	
	}
		
	private static LinkedList<Conjunction> computeConjunctions(HNSet set){

		LinkedList<Conjunction> result = new LinkedList<Conjunction>();
		
		if(set.size() > 0){
		
			int[] indices = new int[set.size()]; 
			for(int i = 0; i < set.size(); i++) indices[i] = set.get(i).size() - 1;
			
			while(indices[0] >= 0){
				
				Conjunction c = new Conjunction();
				for(int i = 0; i < set.size(); i++){
					
					int element = set.get(i).get(indices[i]);
					c.addElement(element);
				}
				result.add(c);
				
				for(int i = set.size() - 1; i >= 0; i--){
					
					indices[i] --;
					if(indices[i] < 0){
						
						if(i > 0) indices[i] = set.get(i).size() - 1;
					}
					else break;
				}
			}
		}
		
		return result;
	}
}
