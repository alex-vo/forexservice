package com.example.forexservice.dto.external

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import jakarta.xml.bind.annotation.XmlAttribute
import java.math.BigDecimal

data class Envelope(
    @JacksonXmlProperty(localName = "Cube")
    var cube: OuterCube? = null
)

data class OuterCube(
    @JacksonXmlElementWrapper(localName = "Cube")
    @JacksonXmlProperty(localName = "Cube")
    var innerCubes: List<InnerCube> = listOf()
)

@JacksonXmlRootElement(localName = "Cube")
data class InnerCube(
    @XmlAttribute
    var currency: String? = null,
    @XmlAttribute
    var rate: BigDecimal? = null,
)
