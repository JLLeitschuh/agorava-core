/*
 * Copyright 2013 Agorava
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.agorava.core.cdi;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterables;
import org.agorava.core.api.ApplyQualifier;
import org.agorava.core.api.Injectable;
import org.agorava.core.api.RemoteServiceRoot;
import org.agorava.core.api.ServiceRelated;
import org.agorava.core.api.exception.AgoravaException;
import org.agorava.core.api.oauth.OAuthAppSettings;
import org.agorava.core.api.oauth.OAuthProvider;
import org.agorava.core.api.oauth.OAuthService;
import org.agorava.core.api.oauth.OAuthSession;
import org.agorava.core.oauth.OAuthSessionImpl;
import org.agorava.core.oauth.scribe.OAuthProviderScribe;
import org.apache.deltaspike.core.util.bean.BeanBuilder;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

/**
 * Agorava CDI extension to discover existing module and configured modules
 *
 * @author Antoine Sabot-Durand
 */
public class AgoravaExtension implements Extension, Serializable {

    private static final Set<Annotation> servicesQualifiersConfigured = newHashSet();
    private static Logger log = Logger.getLogger(AgoravaExtension.class.getName());

    private static BiMap<String, Annotation> servicesToQualifier = HashBiMap.create();
    private static boolean multiSession = false;
    private Map<Annotation, Set<Type>> overridedGenericServices = new HashMap<Annotation, Set<Type>>();

    /**
     * @return the set of all service's names present in the application
     */
    public static Set<String> getSocialRelated() {
        return servicesToQualifier.keySet();
    }

    /**
     * @return a {@link BiMap} associating service annotations (annotation bearing {@link ServiceRelated} meta-annotation) and their name present in the application
     */
    public static BiMap<String, Annotation> getServicesToQualifier() {
        return servicesToQualifier;
    }

    /**
     * @return the set of all service's qualifiers present in the application
     */
    public static Set<Annotation> getServicesQualifiersAvailable() {
        return servicesToQualifier.values();
    }

    /**
     * @return a boolean indicating if the application uses multi sessions or not
     */
    public static boolean isMultiSession() {
        return multiSession;
    }

    public static void setMultiSession(boolean ms) {
        multiSession = ms;
    }

    /**
     * Inspects an annotated element for any annotations with the given meta
     * annotation. This should only be used for user defined meta annotations,
     * where the annotation must be physically present.
     *
     * @param element            The element to inspect
     * @param metaAnnotationType The meta annotation to search for
     * @return The annotation instances found on this element or an empty set if
     *         no matching meta-annotation was found.
     */
    public static Set<Annotation> getAnnotationsWithMeta(Annotated element, final Class<? extends Annotation> metaAnnotationType) {
        Set<Annotation> annotations = new HashSet<Annotation>();
        for (Annotation annotation : element.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(metaAnnotationType)) {
                annotations.add(annotation);
            }
        }
        return annotations;
    }

    /**
     * This observer is started at the beginning of the extension
     *
     * @param bbd
     */
    public void launchExtension(@Observes BeforeBeanDiscovery bbd) {
        log.info("Starting Agorava Framework initialization");
    }

    /**
     * This observer decorates the produced {@link OAuthAppSettings} by injecting its own qualifier and service name
     * and build the list of Qualifiers bearing the ServiceRelated meta annotation (configured services)
     *
     * @param pp the Process producer event
     */
    public void processOAuthSettingsProducer(@Observes final ProcessProducer<?, OAuthAppSettings> pp) {
        final AnnotatedMember<OAuthAppSettings> annotatedMember = (AnnotatedMember<OAuthAppSettings>) pp.getAnnotatedMember();
        final Annotation qual = Iterables.getLast(AgoravaExtension.getAnnotationsWithMeta(annotatedMember, ServiceRelated.class));
        final Producer<OAuthAppSettings> oldProducer = pp.getProducer();

        if (annotatedMember.isAnnotationPresent(OAuthApplication.class)) {
     /*  final OAuthApplication app = annotatedMember.getAnnotation(OAuthApplication.class);

        Class<? extends OAuthAppSettingsBuilder> builderClass = app.builder();
        OAuthAppSettingsBuilder builderOAuthApp = null;
        try {
            builderOAuthApp = builderClass.newInstance();
        } catch (Exception e) {
            throw new AgoravaException("Unable to create Settings Builder with class " + builderClass, e);
        }

        final OAuthAppSettingsBuilder finalBuilderOAuthApp = builderOAuthApp; */
        }

        pp.setProducer(new OAuthAppSettingsProducerDecorator(oldProducer, qual));

        log.log(INFO, "Found settings for {0}", qual);
        servicesQualifiersConfigured.add(qual);

        //settings = builderOAuthApp.name(servicesHub.getSocialMediaName()).params(app.params()).build();
    }

    public void processRemoteServiceRoot(@Observes ProcessBean<RemoteServiceRoot> pb, BeanManager beanManager) {
        CommonsProcessRemoteService(pb, beanManager);
    }

    public void processRemoteServiceRoot(@Observes ProcessProducerMethod<RemoteServiceRoot, ?> pb, BeanManager beanManager) {
        CommonsProcessRemoteService((ProcessBean<RemoteServiceRoot>) pb, beanManager);
    }

    private void CommonsProcessRemoteService(ProcessBean<RemoteServiceRoot> pb, BeanManager beanManager) {
        CreationalContext ctx = beanManager.createCreationalContext(null);
        Annotated annotated = pb.getAnnotated();
        Set<Annotation> qualifiers = AgoravaExtension.getAnnotationsWithMeta(annotated, ServiceRelated.class);
        if (qualifiers.size() != 1)
            throw new AgoravaException("A RemoteService bean should have one and only one service related Qualifier : " + pb.getAnnotated().toString());
        Annotation qual = Iterables.getOnlyElement(qualifiers);
        log.log(INFO, "Found new service related qualifier : {0}", qual);

        Bean<?> beanSoc = pb.getBean();

        final RemoteServiceRoot smah = (RemoteServiceRoot) beanManager.getReference(beanSoc, RemoteServiceRoot.class, ctx);
        String name = smah.getServiceName();
        servicesToQualifier.put(name, qual);

        ctx.release();
    }

    private void processGenericBeanAT(ProcessAnnotatedType<?> pat) {
        Annotated annotated = pat.getAnnotatedType();
        if (annotated.isAnnotationPresent(ApplyQualifier.class))
            pat.veto();
        else {
            Set<Annotation> qualifiers = AgoravaExtension.getAnnotationsWithMeta(annotated, ServiceRelated.class);
            if (qualifiers.size() != 0) {
                if (qualifiers.size() > 1)
                    throw new AgoravaException("Beans with multiple Service Related Qualifier are not supported");
                Annotation qual = Iterables.getOnlyElement(qualifiers);
                if (overridedGenericServices.containsKey(qual))
                    overridedGenericServices.get(qual).addAll(annotated.getTypeClosure());
                else
                    overridedGenericServices.put(qual, new HashSet<Type>(annotated.getTypeClosure()));
            }

        }
    }

    public void processOAuthSessionAT(@Observes ProcessAnnotatedType<OAuthSession> pat) {
        processGenericBeanAT(pat);

    }

    public void processOAuthServiceAT(@Observes ProcessAnnotatedType<OAuthService> pat) {
        processGenericBeanAT(pat);
    }

    public void processOAuthProviderAT(@Observes ProcessAnnotatedType<OAuthProvider> pat) {
        processGenericBeanAT(pat);
    }

    /**
     * After all {@link OAuthAppSettings} were discovered we get their bean to retrieve the actual name of Social Media
     * and associates it with the corresponding Qualifier
     *
     * @param abd
     * @param beanManager
     */
    public void processAfterDeploymentValidation(@Observes AfterBeanDiscovery abd, BeanManager beanManager) {


        for (Annotation qual : servicesQualifiersConfigured) {

            // Register, OAuthProvider, OAuthSession, OAuthService if they don't exist (overloaded) yet
            // Register OAuthProvider
            beanRegisterer(OAuthProviderScribe.class, qual, ApplicationScoped.class, abd, beanManager);
            beanRegisterer(OAuthSessionImpl.class, qual, Dependent.class, abd, beanManager);
            beanRegisterer(OAuthServiceImpl.class, qual, ApplicationScoped.class, abd, beanManager);
        }


        if (servicesQualifiersConfigured.size() != servicesToQualifier.size())
            log.log(WARNING, "Some Service modules present in the application are not configured so won't be available"); //TODO:list the service without config
        log.info("Agorava initialization complete");

    }

    private <T> void beanRegisterer(Class<T> clazz, Annotation qual, Class<? extends Annotation> scope, AfterBeanDiscovery abd, BeanManager beanManager) {

        if (!(overridedGenericServices.containsKey(qual) && overridedGenericServices.get(qual).contains(clazz))) {

            AnnotatedType<T> at = beanManager.createAnnotatedType(clazz);
            AnnotatedTypeBuilder<T> atb = new AnnotatedTypeBuilder<T>()
                    .readFromType(clazz)
                    .setJavaClass(clazz);

            injectify(qual, at, atb);


            BeanBuilder<T> providerBuilder = new BeanBuilder<T>(beanManager)
                    .readFromType(atb.create())
                    .addQualifier(qual)
                    .scope(scope);

            Bean<T> bean = providerBuilder.create();


            abd.addBean(bean);
        }
    }

    void injectify(Annotation qual, AnnotatedType<?> at, AnnotatedTypeBuilder<?> atb) {
        //do a loop on all field to replace annotation mark by CDI annotations
        for (AnnotatedField af : at.getFields())
            if (af.isAnnotationPresent(Injectable.class)) {
                atb.addToField(af, InjectLiteral.instance);
                if (af.isAnnotationPresent(ApplyQualifier.class))
                    atb.addToField(af, qual);
            }

        //loop on constructors to do the same
        for (AnnotatedConstructor ac : at.getConstructors()) {
            if (ac.isAnnotationPresent(Injectable.class)) {

                atb.addToConstructor(ac, InjectLiteral.instance);
                Annotation[][] pa = ac.getJavaMember().getParameterAnnotations();
                //loop on args to detect marked param
                for (int i = 0; i < pa.length; i++)
                    for (int j = 0; j < pa[i].length; j++)
                        if (pa[i][j].equals(ApplyQualifierLiteral.instance))
                            atb.addToConstructorParameter(ac.getJavaMember(), i, qual);
            }
        }

        //loop on other methods (setters)
        for (AnnotatedMethod am : at.getMethods())
            if (am.isAnnotationPresent(Injectable.class)) {
                atb.addToMethod(am, InjectLiteral.instance);
                if (am.isAnnotationPresent(ApplyQualifierLiteral.class))
                    atb.addToMethod(am, qual);
            }

    }

    public <T extends OAuthServiceImpl> void processOauthServiceSons(@Observes ProcessAnnotatedType<T> pat) {
        processGenericSons(pat, OAuthServiceImpl.class);
    }

    public <T extends OAuthProviderScribe> void processOauthProviderSons(@Observes ProcessAnnotatedType<T> pat) {
        processGenericSons(pat, OAuthProviderScribe.class);
    }

    public <T extends OAuthSessionImpl> void processOauthSessionSons(@Observes ProcessAnnotatedType<T> pat) {
        processGenericSons(pat, OAuthSessionImpl.class);
    }

    private <T> void processGenericSons(ProcessAnnotatedType<T> pat, Class belowClass) {
        AnnotatedType<T> at = pat.getAnnotatedType();
        if (!at.getBaseType().equals(belowClass)) {

            Set<Annotation> qualifiers = AgoravaExtension.getAnnotationsWithMeta(at, ServiceRelated.class);
            if (qualifiers.size() != 0) {
                if (qualifiers.size() > 1)
                    throw new AgoravaException("Beans with multiple Service Related Qualifier are not supported");
                Annotation qual = Iterables.getOnlyElement(qualifiers);
                Class<T> clazz = (Class) at.getBaseType();

                AnnotatedTypeBuilder<T> atb = new AnnotatedTypeBuilder<T>()
                        .readFromType(clazz)
                        .setJavaClass(clazz);

                injectify(qual, atb.create(), atb);
                pat.setAnnotatedType(atb.create());
            }

        }
    }
}
