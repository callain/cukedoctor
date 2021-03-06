package com.github.cukedoctor.renderer;

import com.github.cukedoctor.api.CukedoctorDocumentBuilder;
import com.github.cukedoctor.api.model.Feature;
import com.github.cukedoctor.api.model.Scenario;
import com.github.cukedoctor.config.CukedoctorConfig;
import com.github.cukedoctor.spi.FeatureRenderer;
import com.github.cukedoctor.spi.ScenarioRenderer;

import javax.xml.parsers.DocumentBuilder;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import static com.github.cukedoctor.util.Assert.*;
import static com.github.cukedoctor.util.Constants.Markup.bold;
import static com.github.cukedoctor.util.Constants.newLine;

/**
 * Created by pestano on 27/02/16.
 */
public class CukedoctorFeatureRenderer extends AbstractBaseRenderer implements FeatureRenderer {

    ScenarioRenderer scenarioRenderer;

    public CukedoctorFeatureRenderer() {
        ServiceLoader<ScenarioRenderer> scenarioRenderers = ServiceLoader.load(ScenarioRenderer.class);

        if (scenarioRenderers.iterator().hasNext()) {
            scenarioRenderer = scenarioRenderers.iterator().next();
        } else {
            scenarioRenderer = new CukedoctorScenarioRenderer();
            scenarioRenderer.setI18n(i18n);
        }
    }

    @Override
    public String renderFeature(Feature feature) {
        docBuilder.clear();
        if (feature.hasIgnoreDocsTag()) {
            return "";
        }
        docBuilder.textLine(renderFeatureSectionId(feature));
        if(!CukedoctorConfig.hideFeaturesSection()) {//when feature section is not rendered we have to 'downgrade' other sections
            docBuilder.sectionTitleLevel2((bold(feature.getName()))).newLine();
        }else{
            docBuilder.sectionTitleLevel1((bold(feature.getName()))).newLine();
        }
        if (notNull(documentAttributes) && hasText(documentAttributes.getBackend()) && documentAttributes.getBackend().toLowerCase().contains("html")) {
            //used by minimax extension @see com.github.cukedoctor.extension.CukedoctorMinMaxExtension
            docBuilder.append("minmax::", feature.getName().replaceAll(",", "").replaceAll(" ", "-")).append("[]").newLine();
        }
        if (hasText(feature.getDescription())) {
            docBuilder.sideBarBlock(feature.getDescription().trim().replaceAll("\\\\", "").replaceAll("\\n", newLine()));
        }

        if(feature.hasScenarios()){
            docBuilder.append(renderFeatureScenarios(feature));
        }

        return docBuilder.toString();
    }

    @Override
    public String renderFeatures(List<Feature> features) {
        CukedoctorDocumentBuilder parentBuilder = CukedoctorDocumentBuilder.Factory.newInstance();
        if(!CukedoctorConfig.hideFeaturesSection()) {
            parentBuilder.sectionTitleLevel1(bold(i18n.getMessage("title.features"))).newLine();
        }
        for (Feature feature : features) {
            parentBuilder.append(renderFeature(feature));
        }
        return parentBuilder.toString();
    }

    protected String renderFeatureScenarios(Feature feature) {
        StringBuilder sb = new StringBuilder();
        for (Scenario scenario : feature.getScenarios()) {
            sb.append(renderFeatureScenario(scenario, feature));
        }
        feature.setBackgroundRendered(false);
        return sb.toString();
    }

    protected String renderFeatureSectionId(Feature feature) {
        if (isNull(feature) || not(hasText(feature.getName()))) {
            return "";
        }
        //Anchor must not have blanks neither commas to work
        return "[[" + feature.getName().replaceAll(",", "").replaceAll(" ", "-") +
                ", " + feature.getName() + "]]";
    }

    protected String renderFeatureScenario(Scenario scenario, Feature feature){
        return scenarioRenderer.renderScenario(scenario,feature);
    }
}
