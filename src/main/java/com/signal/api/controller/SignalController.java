package com.signal.api.controller;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import com.signal.api.model.Signal;
import com.signal.api.model.TypeSignal;
import com.signal.api.payload.response.MessageResponse;
import com.signal.api.repository.RegionRepository;
import com.signal.api.repository.SignalRepository;
import com.signal.api.repository.TypeSignalRepository;
import com.signal.api.service.FilesStorageService;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.apache.commons.io.FileUtils;
import org.springframework.http.MediaType;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/signal")
public class SignalController {
	
	@Autowired
	SignalRepository signalRepository;
	
	@Autowired
	RegionRepository regionRepository;
	
	@Autowired
	TypeSignalRepository typeSignalRepository;
	
	private Path rootLocation;

	@GetMapping("/all")
	@PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
	public ResponseEntity<?> findAll() {
		return ResponseEntity.ok(signalRepository.findAll());
	}

	@GetMapping("/one/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	public ResponseEntity<?> findById(@PathVariable("id") Long id) {
		return ResponseEntity.ok(signalRepository.findById(id));
	}
	
	//Ho an'ny Region ny moderator
	@GetMapping("/byregion")
	@PreAuthorize("hasRole('MODERATOR')")
	public ResponseEntity<?> getRegion(@RequestBody String username){
		return ResponseEntity.ok(regionRepository.findByUsername(username));
	}
	
	@GetMapping("/findbyregion")
	@PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
	public ResponseEntity<List<Signal>> findByRegion(@Param("nomRegion") String nomRegion){
		if(nomRegion == null){
			return ResponseEntity.ok(null);
		}else if(nomRegion == "tout"){
			return ResponseEntity.ok(signalRepository.findAll());
		}
		return ResponseEntity.ok(signalRepository.findByRegion(nomRegion));
	}

	@GetMapping("/findbyuserpost")
	@PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR') or hasRole('USER')")
	public ResponseEntity<List<Signal>> findByUsername(@Param("usermane") String username){
		return ResponseEntity.ok(signalRepository.findByUsername(username));
	}

	@GetMapping("/client")
	@PreAuthorize("hasRole('MODERATOR') or hasRole('USER')")
	public ResponseEntity<?> findBySeenAndClient(@Param("username") String username){
		return ResponseEntity.ok(signalRepository.findBySeenAndClient(username));
	}

	@PutMapping("/client/seen")
	@PreAuthorize("hasRole('MODERATOR') or hasRole('USER')")
	public ResponseEntity<?> updateSeenByClient(@Param("id") Long id){
		signalRepository.updateSeenByClient(id,1);
		return ResponseEntity.ok(new MessageResponse("Update successfully"));
	}

	@PutMapping("/client/seenno")
	@PreAuthorize("hasRole('MODERATOR') or hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<?> updateSeenByClientno(@Param("id") Long id){
		signalRepository.updateSeenByClientno(id,0);
		return ResponseEntity.ok(new MessageResponse("Update successfully"));
	}

	@PutMapping("/updateRegion/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> updateRegionSignal(@PathVariable("id") Long id, @RequestBody String region){
		signalRepository.updateRegionSignal(region, id);
		return ResponseEntity.ok(new MessageResponse("Update successfully"));
	}

	@PutMapping("/update/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
	public ResponseEntity<?> updateStatusSignal(@PathVariable("id") Long id, @RequestBody String status){
		signalRepository.updateStatusSignal(status, id);
		return ResponseEntity.ok(new MessageResponse("Update successfully"));
	}

	@Autowired
  	FilesStorageService storageService;
	
	  @PostMapping("/postsignal")
	  @PreAuthorize("hasRole('USER')")
	  public ResponseEntity<MessageResponse> uploadFile(
		  @RequestParam("file") MultipartFile file, 
		  @RequestParam("typeSignal") String typeSignal,
		  @RequestParam("description") String description,
		  @RequestParam("latitude") float latitude,
		  @RequestParam("longitude") float longitude,
		  @RequestParam("status") String status,
		  @RequestParam("date") Date date,
		  @RequestParam("username") String username
	  ) {
		  Signal s = new Signal();
		  String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
  
		  rootLocation = Paths.get("signal_images");
  
		  String filename = username+"_"+timeStamp+"."+file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
		  try {
			  Files.copy(file.getInputStream(), this.rootLocation.resolve(filename),
					  StandardCopyOption.REPLACE_EXISTING);
		  } catch (IOException e) {
			  e.printStackTrace();
		  } 
		  
		  List<TypeSignal> _type = new ArrayList<TypeSignal>();
		  TypeSignal type = typeSignalRepository.findByType(typeSignal).get();
		  _type.add(type);
		  s.setImage(filename);
		  s.setDescription(description);
		  s.setTypeSignal(_type);
		  s.setLatitude(latitude);
		  s.setLongitude(longitude);
		  s.setStatus(status);
		  s.setDate(date);
		  s.setUsername(username);
		  s.setSeen(0);
		  signalRepository.save(s);
		  return ResponseEntity.ok(new MessageResponse("Saved successfully"));
	  }
	  
	
	@DeleteMapping("/delete/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	public ResponseEntity<MessageResponse> deleteSignal(@PathVariable Long id){
		signalRepository.deleteById(id);
		return ResponseEntity.ok(new MessageResponse("Signal remove successfully"));
	}

	//root path for image files
    private String FILE_PATH_ROOT = "signal_images/";
	
    @GetMapping("/{filename}")
    public ResponseEntity<byte[]> getImage(@PathVariable("filename") String filename) {
        byte[] image = new byte[0];
        try {
            image = FileUtils.readFileToByteArray(new File(FILE_PATH_ROOT+filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image);
    }
	
}
