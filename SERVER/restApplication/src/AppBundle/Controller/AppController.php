<?php
namespace AppBundle\Controller;

use Sensio\Bundle\FrameworkExtraBundle\Configuration\Route;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\ParameterBag;
use AppBundle\Entity\utente;
use AppBundle\Entity\archi;
use AppBundle\Entity\ocrresult;
use AppBundle\Entity\localizzazioni;
use Doctrine\ORM\Query;
use Doctrine\DBAL\Query\QueryBuilder;
use Doctrine\ORM\Query\ResultSetMapping;
use Symfony\Bundle\FrameworkBundle\Controller\Controller;
use Symfony\Component\Serializer\Serializer;
use Symfony\Component\Serializer\Encoder\XmlEncoder;
use Symfony\Component\Serializer\Encoder\JsonEncoder;
use Symfony\Component\Serializer\Normalizer\GetSetMethodNormalizer;
use SensioLabs\Security\Command\SecurityCheckerCommand;
use Symfony\Component\Validator\Constraints as Assert;
use Doctrine\Common\Annotations\AnnotationReader;



class AppController extends Controller
{
    /**
  * @Route("/provaserver")
  */
    public function showAction()
    {
        return new Response ("il server funziona!");
    }

    
    
    
    // PARTE PROGETTO IM

    //METODO PER REGISTRARE UN OCRRESULT SUL DATABASE
    /**
* @Route("/regocr/{ocrtext}/{ocrbitmap}/{survey}/{operator}/{datetime}/{latitudine}/{longitudine}/{ocrconfidence}")
*/
    public function registraOcr($ocrtext,$ocrbitmap,$survey,$operator,$datetime,$latitudine,$longitudine,$ocrconfidence)
    {
        //http://localhost:8000/regocr/ocrt/bit.png/surv/oper/ciao/3.1/3.2/90
        //registraOcr(ocrresult $ocrres)

        //DA DECOMMENTARE SE IL METODO è registraOcr(ocrresult $ocrres)
        /*

$ocrtext=$ocrres->getOcrtext('ocrtext');      OPPURE  $ocrtext=$ocrres->ocrres->getOcrtext('ocrtext');

    $ocrbitmap=$ocrres->->getOcrbitmap('ocrbitmap');
  $survey=$ocrres->ocrres->getSurvey('survey');
  $operator=$ocrres->ocrres->getOperator('operator');
  $datetime=$ocrres->ocrres->getDatetime('datetime');
  $latitudine=$ocrres->ocrres->getLatitudine('latitudine');
    $longitudine=$ocrres->ocrres->getLongitudine('longitudine');
      $ocrconfidence=$ocrres->ocrres->getOcrconfidence('ocrconfidence');



        */

        $ocrdb = new ocrresult();

        $ocrdb->setOcrtext($ocrtext);
        $ocrdb->setOcrbitmap($ocrbitmap);
        $ocrdb->setSurvey($survey);
        $ocrdb->setOperator($operator);
        $ocrdb->setDatetime($datetime);
        $ocrdb->setLatitudine($latitudine);
        $ocrdb->setLongitudine($longitudine);
        $ocrdb->setOcrconfidence($ocrconfidence);

        $em = $this->getDoctrine()->getEntityManager();
        $em->persist($ocrdb);
        $em->flush();

        $response1 = new Response(json_encode(array('ok' => 'ocr registrato')));
        $response1->headers->set('Content-Type', 'application/json');
        return ($response1);

    }

    
    public function trovaOcrRes($datetime,$ocrtext)
    {
        $criteria1 = array('datetime' => $datetime,
                           'ocrtext' => $ocrtext);
        $em = $this->getDoctrine();
        $query = $em->getRepository('AppBundle:ocrresult')->findOneBy($criteria1);



        
        if($query!=NULL){
           return true;
        }else{
            return false;

        }

    
    }
    
    //METODO PER scrivere un ocr res dentro al db
    public function writeocrRes(ocrresult $ocrres)
    {
        if(!($this->trovaOcrRes($ocrres->getDatetime(),$ocrres->getOcrtext()))){
        $em = $this->getDoctrine()->getEntityManager();
        $em->persist($ocrres);
        $em->flush();}
    }
    
        
        

    
    
    //METODO PER verificare che un ocr res sia dentro al db
    public function checkocrRes($ocrdb)
    {

       $criteria1 = array('datetime' => $datetime,
                           'ocrtext' => $ocrtext);
        $em = $this->getDoctrine();
        $query = $em->getRepository('AppBundle:ocrresult')->findOneBy($criteria1);



        
        if($query!=NULL){
           return true;
        }else{
            return false;

        }

    }
    
    
    
    /**
  * @Route("/sincronizza")
  */
    
    /* public function sincronizza(Request $request){
         
         if($request->getmethod() == 'POST')
         {
            $data = json_decode($request->getContent(), true);
            $request->request->replace($data);
            //$meanconfidences =  $request->request->get('meanconfidence');
            //$bitmaps = $request->request->get('ocrbitmap');
            $ocrtexts = $request->request->get('ocrtext');
            $meanconfidences = $request->request->get('ocrconfidence');
            $latitudes = $request->request->get('latitudine');
            $longitudes = $request->request->get('longitudine');
            $datetimes = $request->request->get('datetime');
            $surveys = $request->request->get('survey');
            $operators = $request->request->get('operator');
            $bitmaps = $request->request->get('ocrbitmap');
            
            if(is_array($ocrtexts) && count($ocrtexts)>0)
                {
                    try{
                        for($i=0; $i< count($ocrtexts);$i++){

                            $ocrdb = new ocrresult();

                            $ocrdb->setOcrtext($ocrtexts[$i]);
                            $ocrdb->setOcrbitmap(base64_decode($bitmaps[$i]));
                            $ocrdb->setSurvey($surveys[$i]);
                            $ocrdb->setOperator($operators[$i]);
                            $ocrdb->setDatetime($datetimes[$i]);
                            $ocrdb->setLatitudine($latitudes[$i]);
                            $ocrdb->setLongitudine($longitudes[$i]);
                            $ocrdb->setOcrconfidence($meanconfidences[$i]);

                           if($this->checkocrRes($ocrdb) === FALSE){
                               
                               
                               $this->writeocrRes($ocrdb);
                           }
                            
                            $m='dati sincronizzati';
                        }
                        
                    }catch(Exception $e){
                        //$m=var_dump($this->checkocrRes($ocrdb));
                    }
                
                
                $response1 = new Response(json_encode(array('stato' => 'OK','risultato' =>$m)));
                    

                }else{
                $m=var_dump($data);
                $response1 = new Response(json_encode(array('stato' => 'KO','risultato' =>$m)));
            }
                
                
             
            
         }else
         {
            $m='DEVI USARE UNA POST';
            $response1 = new Response(json_encode(array('stato' => 'KO','risultato' =>$m)));
         }
         
         return $response1;
         
         
     }*/
    
        
        /**
      * @Route("/sincronizza")
      */
    
        public function sincronizza(Request $request){
         
         if($request->getmethod() == 'POST')
         {
            $data = json_decode($request->getContent(), true);
            $request->request->replace($data);
            //$meanconfidences =  $request->request->get('meanconfidence');
            //$bitmaps = $request->request->get('ocrbitmap');
            $ocrtexts = $request->request->get('ocrtext');
            $meanconfidences = $request->request->get('ocrconfidence');
            $latitudes = $request->request->get('latitudine');
            $longitudes = $request->request->get('longitudine');
            $datetimes = $request->request->get('datetime');
            $surveys = $request->request->get('survey');
            $operators = $request->request->get('operator');
            $bitmaps = $request->request->get('ocrbitmap');
            
            if(is_array($ocrtexts) && count($ocrtexts)>0)
                {
                    
                        for($i=0; $i< count($ocrtexts);$i++){

                            $ocrdb = new ocrresult();

                            $ocrdb->setOcrtext($ocrtexts[$i]);
                            $ocrdb->setOcrbitmap(base64_decode($bitmaps[$i]));
                            $ocrdb->setSurvey($surveys[$i]);
                            $ocrdb->setOperator($operators[$i]);
                            $ocrdb->setDatetime($datetimes[$i]);
                            $ocrdb->setLatitudine($latitudes[$i]);
                            $ocrdb->setLongitudine($longitudes[$i]);
                            $ocrdb->setOcrconfidence($meanconfidences[$i]);

                           
                            
                            $this->writeocrRes($ocrdb);
                           
                               
                        
                        }
                           
                            
                    $m='dati sincronizzati';





                    $response1 = new Response(json_encode(array('stato' => 'OK','risultato' =>$m)));
                    

                }else{
                $m=var_dump($data);
                $response1 = new Response(json_encode(array('stato' => 'KO','risultato' =>$m)));
            }
                
                
             
            
         }else
         {
            $m='DEVI USARE UNA POST';
            $response1 = new Response(json_encode(array('stato' => 'KO','risultato' =>$m)));
         }
         
         return $response1;
         
         
     }
    
    
}
         