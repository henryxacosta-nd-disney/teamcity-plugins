package com.goldin.plugins.teamcity.report

import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.serverSide.SBuildServer
import jetbrains.buildServer.serverSide.ServerPaths
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.WebControllerManager
import org.springframework.context.ApplicationContext
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class ReportController extends BaseController
{
    static final String         MAPPING      = 'displayReport.html'
    private static final String JAVADOC_BASE = 'http://javadoc.jetbrains.net/teamcity/openapi/current'

    private final SBuildServer       server
    private final ApplicationContext context
    private final PluginDescriptor   descriptor
    private final ServerPaths        paths


    ReportController ( SBuildServer         server,
                       WebControllerManager manager,
                       ApplicationContext   context,
                       PluginDescriptor     descriptor,
                       ServerPaths          paths )
    {
        super( server )
        this.server     = server
        this.context    = context
        this.descriptor = descriptor
        this.paths      = paths
        manager.registerController( "/$MAPPING", this )
    }


    @Override
    protected ModelAndView doHandle ( HttpServletRequest  httpServletRequest,
                                      HttpServletResponse httpServletResponse )
    {
        final model       = []
        final serverTable = [:]
        final pathsTable  = [:]
        final link        = {
            Class c, boolean useSimpleName = true ->
            "<a href='$JAVADOC_BASE/${ c.name.replace( '.', '/' )}.html'>${ useSimpleName ? c.simpleName : c.name }</a>"
        }

        serverTable[ 'TeamCity Version' ] = server.fullServerVersion
        serverTable[ 'Root Path'        ] = server.serverRootPath

        model << [ link( SBuildServer ), serverTable ]

        pathsTable[ 'Artifacts'     ] = paths.artifactsDirectory.canonicalPath
        pathsTable[ 'Backups'       ] = paths.backupDir
        pathsTable[ 'Caches'        ] = paths.cachesDir
        pathsTable[ 'Configuration' ] = paths.configDir
        pathsTable[ 'Data'          ] = paths.dataDirectory.canonicalPath
        pathsTable[ 'Lib'           ] = paths.libDir
        pathsTable[ 'Logs'          ] = paths.logsPath.canonicalPath
        pathsTable[ 'Plugins Data'  ] = paths.pluginDataDirectory
        pathsTable[ 'Plugins'       ] = paths.pluginsDir
        pathsTable[ 'System'        ] = paths.systemDir

        model << [ link( ServerPaths ), pathsTable ]

        final openApiClasses = this.class.getResource( '/open-api-classes.txt' ).
                               getText( 'UTF-8' ).readLines()*.trim().toSet()

        for ( ApplicationContext c = context; c; c = c.parent )
        {
            final contextMap = c.getBeanNamesForType( Object ).sort().inject([:]){
                Map m, String beanName ->
                //noinspection GroovyGetterCallCanBePropertyAccess
                final beanClass     = c.getBean( beanName ).getClass()
                final beanClassName = beanClass.name
                m[ beanName ]       = beanClassName in openApiClasses ? link( beanClass, false ) : beanClassName
                m
            }

            final title = ( c == context ) ? 'Spring Context' : 'Parent Spring Context'
            model << [ "<a href='http://static.springsource.org/spring/docs/3.0.x/javadoc-api/org/springframework/context/ApplicationContext.html'>$title</a>",
                       contextMap ]
        }

        new ModelAndView( descriptor.getPluginResourcesPath( 'displayReport.jsp' ),
                          [ tables: model ] )
    }
}