package com.goldin.plugins.teamcity.messenger.extension

import javax.servlet.http.HttpServletRequest
import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PlaceId
import jetbrains.buildServer.web.openapi.SimplePageExtension
import jetbrains.buildServer.web.util.WebUtil
import org.jetbrains.annotations.NotNull
import jetbrains.buildServer.web.openapi.PositionConstraint
import com.goldin.plugins.teamcity.messenger.Constants

/**
 * Messenger extension
 */
class MessageDisplayExtension extends SimplePageExtension
{
    MessageDisplayExtension ( PagePlaces pagePlaces, Constants constants )
    {
        super( pagePlaces, PlaceId.ALL_PAGES_HEADER, constants.pluginName, 'messagesDisplay.jsp' )
        position = PositionConstraint.first()
        register()
    }

    @Override
    boolean isAvailable ( @NotNull final HttpServletRequest request )
    {
        def path = WebUtil.getPathWithoutAuthenticationType( request )
        (( path == '/' ) || path.startsWith( '/overview.html' ))
    }
}
