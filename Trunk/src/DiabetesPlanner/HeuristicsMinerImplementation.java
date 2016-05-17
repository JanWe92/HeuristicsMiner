package DiabetesPlanner;

import java.io.File;
import java.io.IOException;
import java.util.*;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.in.XesXmlGZIPParser;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.*;
import org.processmining.models.heuristics.impl.ActivitiesMappingStructures;
import org.processmining.models.heuristics.impl.HeuristicsNetImpl;
import org.processmining.plugins.heuristicsnet.SimpleHeuristicsNet;
import org.processmining.plugins.heuristicsnet.miner.heuristics.HeuristicsMetrics;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.HeuristicsMiner;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.settings.HeuristicsMinerSettings;

/**
 * Created by Jan on 17.05.16.
 */
public class HeuristicsMinerImplementation {

    public static void main(String[] args) throws IOException {
        try {
            String logFile = "/Users/Jan/Documents/Development/FuzzyMiner/input/activityDataCompleteWithCasesJoined2.xes.gz";
            XesXmlGZIPParser xesXmlGZIPParser = new XesXmlGZIPParser();
            File file = new File(logFile);
            System.out.println(xesXmlGZIPParser.canParse(file));
            List<XLog> logs = xesXmlGZIPParser.parse(file);
            XLog log = logs.get(0);

            XLogInfo logInfo = XLogInfoFactory.createLogInfo(log);
            XEventClassifier classifier = logInfo.getEventClassifiers().iterator().next();
            HeuristicsMinerSettings hms = new HeuristicsMinerSettings();
            hms.setClassifier(classifier);

            XEventClasses EventClasses = XEventClasses.deriveEventClasses(classifier,log);
            ActivitiesMappingStructures struct = new ActivitiesMappingStructures(EventClasses);

            HeuristicsMiner miner = new HeuristicsMiner(null,log,logInfo,hms);
            miner.mine();
            SimpleHeuristicsNet HMNet = new SimpleHeuristicsNet(miner.makeBasicRelations(new HeuristicsMetrics(logInfo)),new HeuristicsMetrics(logInfo),hms);
            HeuristicsNetImpl HeuristicsNet = new HeuristicsNetImpl(struct);
        }catch (Exception e)
        {
            e.getMessage();
        }
    }
}
