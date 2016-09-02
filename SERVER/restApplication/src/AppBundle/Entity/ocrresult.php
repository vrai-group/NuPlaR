<?php
// src/AppBundle/Entity/ocrresult.php


namespace AppBundle\Entity;

use Doctrine\ORM\Mapping as ORM;


/**
 * @ORM\Entity
 * @ORM\Table(name="ocrresult")
 */
class ocrresult
{



        /**
     * @ORM\Column(type="string")
         */
        private $ocrtext;

    /**
     * @ORM\Column(type="string")
         */
    private $ocrbitmap;

    /**
     * @ORM\Column(type="string")
     */
    private $survey;


    /**
     * @ORM\Column(type="string")
     */
    private $operator;
    /**
     * @ORM\Column(type="string")
     * @ORM\Id
     */
    private $datetime;

    /**
     * @ORM\Column(type="float")
     */
    private $latitudine;

    /**
     * @ORM\Column(type="float")
     */
    private $longitudine;

    /**
     * @ORM\Column(type="integer")
     */
    private $ocrconfidence;


    /**
     * Set ocrtext
     *
     * @param string $ocrtext
     *
     * @return ocrresult
     */
    public function setOcrtext($ocrtext)
    {
        $this->ocrtext = $ocrtext;

        return $this;
    }

    /**
     * Get ocrtext
     *
     * @return string
     */
    public function getOcrtext()
    {
        return $this->ocrtext;
    }

    /**
     * Set ocrbitmap
     *
     * @param string $ocrbitmap
     *
     * @return ocrresult
     */
    public function setOcrbitmap($ocrbitmap)
    {
        $this->ocrbitmap = $ocrbitmap;

        return $this;
    }

    /**
     * Get ocrbitmap
     *
     * @return string
     */
    public function getOcrbitmap()
    {
        return $this->ocrbitmap;
    }

    /**
     * Set survey
     *
     * @param string $survey
     *
     * @return ocrresult
     */
    public function setSurvey($survey)
    {
        $this->survey = $survey;

        return $this;
    }

    /**
     * Get survey
     *
     * @return string
     */
    public function getSurvey()
    {
        return $this->survey;
    }

    /**
     * Set operator
     *
     * @param string $operator
     *
     * @return ocrresult
     */
    public function setOperator($operator)
    {
        $this->operator = $operator;

        return $this;
    }

    /**
     * Get operator
     *
     * @return string
     */
    public function getOperator()
    {
        return $this->operator;
    }

    /**
     * Set datetime
     *
     * @param string $datetime
     *
     * @return ocrresult
     */
    public function setDatetime($datetime)
    {
        $this->datetime = $datetime;

        return $this;
    }

    /**
     * Get datetime
     *
     * @return string
     */
    public function getDatetime()
    {
        return $this->datetime;
    }

    /**
     * Set latitudine
     *
     * @param float $latitudine
     *
     * @return ocrresult
     */
    public function setLatitudine($latitudine)
    {
        $this->latitudine = $latitudine;

        return $this;
    }

    /**
     * Get latitudine
     *
     * @return float
     */
    public function getLatitudine()
    {
        return $this->latitudine;
    }

    /**
     * Set longitudine
     *
     * @param float $longitudine
     *
     * @return ocrresult
     */
    public function setLongitudine($longitudine)
    {
        $this->longitudine = $longitudine;

        return $this;
    }

    /**
     * Get longitudine
     *
     * @return float
     */
    public function getLongitudine()
    {
        return $this->longitudine;
    }

    /**
     * Set ocrconfidence
     *
     * @param integer $ocrconfidence
     *
     * @return ocrresult
     */
    public function setOcrconfidence($ocrconfidence)
    {
        $this->ocrconfidence = $ocrconfidence;

        return $this;
    }

    /**
     * Get ocrconfidence
     *
     * @return integer
     */
    public function getOcrconfidence()
    {
        return $this->ocrconfidence;
    }
}
