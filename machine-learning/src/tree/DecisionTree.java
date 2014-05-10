package tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pair.Pair;

import data.attribute.Attribute;
import data.attribute.AttributeSet;
import data.instance.Instance;

/**
 * Encapsulates a learned decision tree.
 * 
 * @author Matthew Bernstein - matthewb@cs.wisc.edu
 *
 */
public class DecisionTree 
{   
    /**
     * The root node of the tree
     */
	private DtNode root = null;
	
	/**
	 * A mapping of node ID to node objects 
	 */
	private Map<Integer, DtNode> nodeSet;
	
	/**
	 * The set of attributes used in the learning of the tree
	 */
	private AttributeSet attributeSet;
	
	/**
	 * The class attribute that the tree predicts
	 */
	private Attribute classAttribute;
	
	/**
	 * Used for assinging unique node Id's to new nodes
	 */
	private int nodeCount = 0;
	
	/**
	 * Constructor
	 * 	
	 * @param attributeSet set of attributes used in learning
	 * @param classAttribute the class attribute this tree predicts
	 */
	public DecisionTree(Attribute classAttribute)
	{
		nodeSet = new HashMap<Integer, DtNode>();
		this.classAttribute = classAttribute;
	}
	
	/**
	 * Add a node to the tree
	 * @param newNode
	 */
	protected void addNode(DtNode newNode)
	{
		newNode.setNodeId(nodeCount);
		nodeSet.put(nodeCount, newNode);
		nodeCount++;
	}
	
	/**
	 * @return the tree's root node
	 */
	public Node getRoot()
	{
		return root;
	}
	
	/**
	 * Get a node with specific ID
	 * 
	 * @param nodeId the node's ID
	 * @return the node
	 */
	public Node getNode(Integer nodeId)
	{
		return nodeSet.get(nodeId);
	}
	
	/**
	 * Set the tree's root node
	 * 
	 * @param rootNode the root node
	 */
	protected void setRoot(DtNode rootNode)
	{
		root = rootNode;
		root.setNodeId(nodeCount);
		nodeSet.put(nodeCount, rootNode);
		nodeCount++;
	}
	
	
	
	@Override
	public String toString()
	{
	    String result = "";
	    
		@SuppressWarnings("unchecked")
		Set<DtNode> children = ((Set<DtNode>) ((Set<?>) root.getChildren()));
		List<DtNode> childrenList = new ArrayList<DtNode>(children);
		Collections.sort(childrenList, DtNode.DTNODE_ORDER);
		
		for (Node child : childrenList)
		{
			StringGenerator strGen = new StringGenerator(this.attributeSet, 
			                                             this.classAttribute);
			result +=  strGen.print( (DtNode) child, 0);
		}
		
		return result;
	}
	
	/**
	 * A private helper class used for traversing the tree recursively
	 * in order to print the print the tree to standard output
	 * 
	 * @author Matthew Bernstein - matthewb@cs.wisc.edu
	 */
	private static class StringGenerator
	{
		@SuppressWarnings("unused")
		private AttributeSet attributeSet;
		private Attribute classAttribute;
		
		public StringGenerator(AttributeSet attributeSet, 
		                           Attribute classAttribute)
		{
			this.attributeSet = attributeSet;
			this.classAttribute = classAttribute;
		}
		
		public String print(DtNode node, Integer depth)
		{
		    String result = "";
		    
			// Print the indentated "|" characters
			for (int i = 0; i < depth; i++)
			{
			    result += "|     ";
			}
			
			// Print the value at the current node
			result += node;
			
			if (node instanceof DtLeaf)
			{
			    result += ": ";
				String classLabel = classAttribute.getNominalValueName(((DtLeaf) node).getClassLabel());
				result += classLabel;
			}
			
			result += "\n";
			
			@SuppressWarnings("unchecked")
			Set<DtNode> children = ((Set<DtNode>) ((Set<?>) node.getChildren()));
			List<DtNode> childrenList = new ArrayList<DtNode>(children);
			Collections.sort(childrenList, DtNode.DTNODE_ORDER);
			
			for (Node child : childrenList)
			{
				DtNode dtChild = (DtNode) child;
				result += print (dtChild, depth+1);
			}
			
			return result;
		}
	}
	
	/**
	 * Classify an instance using this decision tree.
	 * 
	 * @param instance the instance to be classified
	 * @return the nominal value ID of the class attribute predicted for the
	 * given instance
	 */
	public Pair<Integer, Double> classifyInstance(Instance instance)
	{
	    DtNode currNode = (DtNode) this.getRoot();
        
        while (!(currNode instanceof DtLeaf))
        {
            @SuppressWarnings("unchecked")
            Set<DtNode> children = ((Set<DtNode>) ((Set<?>) currNode.getChildren()));
        
            for (DtNode node : children)
            {
                if (node.doesInstanceSatisfyNode(instance))
                {
                    currNode = node;
                    break;
                }
            }
        }

        DtLeaf leaf = (DtLeaf) currNode;
        
        Integer prediction = leaf.getClassLabel().intValue();
        
        /*
         *  Get total number of instances that reached the leaf node 
         */
        Integer totalCount = 0;
        for (Integer count : leaf.getClassCounts().values())
        {
            totalCount += count;
        }
        
        /*
         * Calculate the confidence
         */
        Double confidence = (double) (leaf.getClassCounts().get(prediction)) / totalCount;
      
        return  new Pair<Integer, Double>(prediction, confidence);        
	}
}