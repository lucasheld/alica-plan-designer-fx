package de.uni_kassel.vs.cn.planDesigner.alica.util;

import de.uni_kassel.vs.cn.planDesigner.alica.*;
import de.uni_kassel.vs.cn.planDesigner.alica.configuration.Configuration;
import de.uni_kassel.vs.cn.planDesigner.alica.xml.EMFModelUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by marci on 25.11.16.
 * <p>
 * This class functions as backend for the repository view.
 * This class contains Lists of all Plans, PlanTypes, Behaviours and Tasks
 */
public class AllAlicaFiles {

    private static AllAlicaFiles instance;

    private ObservableList<Pair<Plan, Path>> plans;

    private ObservableList<Pair<PlanType, Path>> planTypes;

    private ObservableList<Pair<Behaviour, Path>> behaviours;

    private Pair<List<Task>, Path> tasks;


    private List<Pair<TaskRepository, Path>> taskRepository;

    public static AllAlicaFiles getInstance() {
        if (instance == null) {
            instance = new AllAlicaFiles();
            try {
                instance.init();
            } catch (URISyntaxException | IOException e) {
                throw new RuntimeException(e);
            }
        }

        return instance;
    }

    public List<Pair<TaskRepository, Path>> getTaskRepository() {
        return taskRepository;
    }

    public ObservableList<Pair<Plan, Path>> getPlans() {
        return plans;
    }

    public ObservableList<Pair<PlanType, Path>> getPlanTypes() {
        return planTypes;
    }

    public Pair<List<Task>, Path> getTasks() {
        return tasks;
    }

    public ObservableList<Pair<Behaviour, Path>> getBehaviours() {
        return behaviours;
    }

    public void init() throws URISyntaxException, IOException {
        Configuration configuration = new Configuration();
        String plansPath = configuration.getPlansPath();
        plans = getRepositoryOf(plansPath, "pml");

        behaviours = getRepositoryOf(plansPath, "beh");

        planTypes = getRepositoryOf(plansPath, "pty");

        taskRepository = getRepositoryOf(configuration.getMiscPath(), "tsk");

        tasks = new Pair<>(taskRepository.get(0).getKey().getTasks(), taskRepository.get(0).getValue());
        EcoreUtil.resolveAll(EMFModelUtils.getAlicaResourceSet());
    }

    public Path getPathForAbstractPlan(AbstractPlan abstractPlan) {
        if (abstractPlan instanceof Plan) {
            return getPlans()
                    .stream()
                    .filter(e -> e.getKey().equals(abstractPlan))
                    .findFirst().get().getValue();

        }

        if (abstractPlan instanceof Behaviour) {
            return getBehaviours()
                    .stream()
                    .filter(e -> e.getKey().equals(abstractPlan))
                    .findFirst().get().getValue();
        }

        if (abstractPlan instanceof PlanType) {
            return getPlanTypes()
                    .stream()
                    .filter(e -> e.getKey().equals(abstractPlan))
                    .findFirst().get().getValue();
        }
        return null;
    }

    private <T extends EObject> ObservableList<Pair<T, Path>> getRepositoryOf(String plansPath, String filePostfix) throws IOException {
        List<Pair<T, Path>> collectedList = Files.walk(Paths.get(plansPath))
                .filter(p -> p.toString().endsWith("." + filePostfix))
                .map(p -> {
                    try {
                        Pair<T, Path> tPathPair = new Pair<>(EMFModelUtils.<T>loadAlicaFileFromDisk(p.toFile()), p);
                        for (Iterator k = tPathPair.getKey().eCrossReferences().iterator(); k.hasNext(); ) {
                            k.next();
                        }
                        return tPathPair;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
        return FXCollections.observableList(collectedList);
    }
}